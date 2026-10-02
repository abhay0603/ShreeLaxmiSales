package com.shreelaxmisales.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private EditText itemName;
    private EditText quantity;
    private EditText rate;

    private TextView totalText;
    private TextView invoicePreview;
    private TextView itemCountText;
    private TextView lastItemText;

    private Button btnMultiply;
    private Button btnNextItem;

    private final ArrayList<BillItem> billItems = new ArrayList<>();

    private double grandTotal = 0.0;

    /*
     * This is the field that receives keypad input.
     * Normally it is Quantity or Rate.
     */
    private EditText activeField;

    /*
     * Calculator state.
     * These buttons remain available, but they are secondary
     * to the normal Quantity × Rate → NEXT workflow.
     */
    private String calculatorOperator = "";
    private double calculatorValue = 0.0;
    private boolean waitingForSecondValue = false;

    private SharedPreferences preferences;

    private static final String PREFS = "ShreeLaxmiSalesPrefs";
    private static final String LAST_MONTH = "last_month";
    private static final String LAST_NUMBER = "last_number";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // ---------------------------------------------------------
        // FIND VIEWS
        // ---------------------------------------------------------

        itemName = findViewById(R.id.itemName);
        quantity = findViewById(R.id.quantity);
        rate = findViewById(R.id.rate);

        totalText = findViewById(R.id.totalText);
        invoicePreview = findViewById(R.id.invoicePreview);
        itemCountText = findViewById(R.id.itemCountText);
        lastItemText = findViewById(R.id.lastItemText);

        btnMultiply = findViewById(R.id.btnMultiply);
        btnNextItem = findViewById(R.id.btnNextItem);

        preferences =
                getSharedPreferences(
                        PREFS,
                        MODE_PRIVATE
                );

        // ---------------------------------------------------------
        // INITIAL SETUP
        // ---------------------------------------------------------

        setupInvoiceNumber();

        setupNumericFields();

        setupKeypad();

        setupMultiplyButton();

        setupNextItemButton();

        setupBillButtons();

        focusQuantity();
    }

    // =============================================================
    // NUMERIC FIELD SETUP
    // =============================================================

    private void setupNumericFields() {

        /*
         * IMPORTANT:
         * Do NOT show the Android keyboard for Quantity/Rate.
         * The app's own keypad is used instead.
         */
        quantity.setShowSoftInputOnFocus(false);
        rate.setShowSoftInputOnFocus(false);

        quantity.setCursorVisible(false);
        rate.setCursorVisible(false);

        quantity.setOnFocusChangeListener(
                (view, hasFocus) -> {

                    if (hasFocus) {
                        activeField = quantity;
                    }
                }
        );

        rate.setOnFocusChangeListener(
                (view, hasFocus) -> {

                    if (hasFocus) {
                        activeField = rate;
                    }
                }
        );

        quantity.setOnClickListener(
                view -> {

                    activeField = quantity;

                    hideKeyboard();
                }
        );

        rate.setOnClickListener(
                view -> {

                    activeField = rate;

                    hideKeyboard();
                }
        );
    }

    private void focusQuantity() {

        rate.clearFocus();

        quantity.requestFocus();

        activeField = quantity;

        hideKeyboard();

        quantity.setSelection(
                quantity.length()
        );
    }

    private void focusRate() {

        quantity.clearFocus();

        rate.requestFocus();

        activeField = rate;

        hideKeyboard();

        rate.setSelection(
                rate.length()
        );
    }

    private void hideKeyboard() {

        View view = getCurrentFocus();

        if (view == null) {
            return;
        }

        InputMethodManager manager =
                (InputMethodManager)
                        getSystemService(
                                Context.INPUT_METHOD_SERVICE
                        );

        if (manager != null) {

            manager.hideSoftInputFromWindow(
                    view.getWindowToken(),
                    0
            );
        }
    }

    // =============================================================
    // LARGE × BUTTON
    // =============================================================

    private void setupMultiplyButton() {

        btnMultiply.setOnClickListener(
                view -> {

                    /*
                     * × is primarily a BILLING button.
                     *
                     * Quantity → × → Rate
                     */

                    String value =
                            quantity.getText()
                                    .toString()
                                    .trim();

                    if (value.isEmpty()) {

                        quantity.setText("0");
                    }

                    /*
                     * Store the quantity for the optional
                     * calculator operation as well.
                     */
                    calculatorValue =
                            getNumber(quantity);

                    calculatorOperator = "*";

                    waitingForSecondValue = true;

                    focusRate();
                }
        );
    }

    // =============================================================
    // NEXT ITEM
    // =============================================================

    private void setupNextItemButton() {

        btnNextItem.setOnClickListener(
                view -> addCurrentItem()
        );
    }

    private void addCurrentItem() {

        double qty =
                getNumber(quantity);

        double price =
                getNumber(rate);

        if (qty <= 0) {

            Toast.makeText(
                    this,
                    "Enter quantity first",
                    Toast.LENGTH_SHORT
            ).show();

            focusQuantity();

            return;
        }

        if (price < 0) {

            Toast.makeText(
                    this,
                    "Enter a valid rate",
                    Toast.LENGTH_SHORT
            ).show();

            focusRate();

            return;
        }

        double itemTotal =
                qty * price;

        String name =
                itemName.getText()
                        .toString()
                        .trim();

        if (name.isEmpty()) {

            name =
                    "Item "
                            + (billItems.size() + 1);
        }

        BillItem item =
                new BillItem(
                        name,
                        qty,
                        price,
                        itemTotal
                );

        billItems.add(item);

        grandTotal += itemTotal;

        updateTotal();

        updateItemStatus(
                qty,
                price,
                itemTotal
        );

        clearCurrentItem();

        /*
         * Immediately ready for next quantity.
         */
        focusQuantity();
    }

    private void updateItemStatus(
            double qty,
            double price,
            double itemTotal
    ) {

        int count =
                billItems.size();

        itemCountText.setText(
                count
                        + " item"
                        + (count == 1 ? "" : "s")
                        + " added"
        );

        lastItemText.setText(
                formatNumber(qty)
                        + " × ₹"
                        + formatNumber(price)
                        + " = ₹"
                        + formatMoney(itemTotal)
        );
    }

    private void clearCurrentItem() {

        itemName.setText("");

        quantity.setText("0");

        rate.setText("0");

        calculatorOperator = "";

        calculatorValue = 0.0;

        waitingForSecondValue = false;
    }

    // =============================================================
    // KEYPAD
    // =============================================================

    private void setupKeypad() {

        // Numbers
        setNumberButton(R.id.btn0, "0");
        setNumberButton(R.id.btn1, "1");
        setNumberButton(R.id.btn2, "2");
        setNumberButton(R.id.btn3, "3");
        setNumberButton(R.id.btn4, "4");
        setNumberButton(R.id.btn5, "5");
        setNumberButton(R.id.btn6, "6");
        setNumberButton(R.id.btn7, "7");
        setNumberButton(R.id.btn8, "8");
        setNumberButton(R.id.btn9, "9");

        // Double zero
        setNumberButton(R.id.btn00, "00");

        // Decimal
        findViewById(R.id.btnDecimal)
                .setOnClickListener(
                        view -> appendDecimal()
                );

        // Sign
        findViewById(R.id.btnSign)
                .setOnClickListener(
                        view -> toggleSign()
                );

        // Backspace
        findViewById(R.id.btnBackspace)
                .setOnClickListener(
                        view -> backspace()
                );

        // Clear
        findViewById(R.id.btnClear)
                .setOnClickListener(
                        view -> clearActiveField()
                );

        // Percentage
        findViewById(R.id.btnPercent)
                .setOnClickListener(
                        view -> percentage()
                );

        // Calculator operators
        findViewById(R.id.btnPlus)
                .setOnClickListener(
                        view -> chooseOperator("+")
                );

        findViewById(R.id.btnMinus)
                .setOnClickListener(
                        view -> chooseOperator("-")
                );

        findViewById(R.id.btnDivide)
                .setOnClickListener(
                        view -> chooseOperator("/")
                );

        /*
         * The large × button is already connected separately
         * because it has the special billing behavior.
         */

        // Equals
        findViewById(R.id.btnEquals)
                .setOnClickListener(
                        view -> calculateResult()
                );
    }

    private void setNumberButton(
            int buttonId,
            String value
    ) {

        findViewById(buttonId)
                .setOnClickListener(
                        view -> appendNumber(value)
                );
    }

    private void appendNumber(String number) {

        if (activeField == null) {

            focusQuantity();
        }

        if (waitingForSecondValue) {

            activeField.setText(number);

            waitingForSecondValue = false;

        } else {

            String current =
                    activeField.getText()
                            .toString();

            if (current.equals("0")) {

                current = "";
            }

            activeField.setText(
                    current + number
            );
        }

        activeField.setSelection(
                activeField.length()
        );
    }

    private void appendDecimal() {

        if (activeField == null) {

            focusQuantity();
        }

        String current =
                activeField.getText()
                        .toString();

        if (!current.contains(".")) {

            if (current.isEmpty()) {

                current = "0";
            }

            activeField.setText(
                    current + "."
            );

            activeField.setSelection(
                    activeField.length()
            );
        }
    }

    private void toggleSign() {

        if (activeField == null) {

            focusQuantity();
        }

        String current =
                activeField.getText()
                        .toString();

        if (current.isEmpty()
                || current.equals("0")) {

            return;
        }

        if (current.startsWith("-")) {

            activeField.setText(
                    current.substring(1)
            );

        } else {

            activeField.setText(
                    "-" + current
            );
        }

        activeField.setSelection(
                activeField.length()
        );
    }

    private void backspace() {

        if (activeField == null) {

            focusQuantity();
        }

        String current =
                activeField.getText()
                        .toString();

        if (current.length() <= 1) {

            activeField.setText("0");

            return;
        }

        activeField.setText(
                current.substring(
                        0,
                        current.length() - 1
                )
        );

        activeField.setSelection(
                activeField.length()
        );
    }

    private void clearActiveField() {

        if (activeField == null) {

            focusQuantity();
        }

        activeField.setText("0");

        calculatorOperator = "";

        calculatorValue = 0.0;

        waitingForSecondValue = false;
    }

    // =============================================================
    // SECONDARY CALCULATOR FUNCTIONS
    // =============================================================

    private void chooseOperator(
            String operator
    ) {

        if (activeField == null) {

            focusQuantity();
        }

        calculatorValue =
                getActiveFieldValue();

        calculatorOperator =
                operator;

        waitingForSecondValue = true;
    }

    private void calculateResult() {

        if (activeField == null) {

            focusQuantity();
        }

        if (calculatorOperator.isEmpty()) {

            return;
        }

        double secondValue =
                getActiveFieldValue();

        double result;

        switch (calculatorOperator) {

            case "+":

                result =
                        calculatorValue
                                + secondValue;

                break;

            case "-":

                result =
                        calculatorValue
                                - secondValue;

                break;

            case "*":

                result =
                        calculatorValue
                                * secondValue;

                break;

            case "/":

                if (secondValue == 0) {

                    Toast.makeText(
                            this,
                            "Cannot divide by zero",
                            Toast.LENGTH_SHORT
                    ).show();

                    return;
                }

                result =
                        calculatorValue
                                / secondValue;

                break;

            default:

                return;
        }

        activeField.setText(
                formatNumber(result)
        );

        activeField.setSelection(
                activeField.length()
        );

        calculatorValue = result;

        calculatorOperator = "";

        waitingForSecondValue = false;
    }

    private void percentage() {

        if (activeField == null) {

            focusQuantity();
        }

        double value =
                getActiveFieldValue();

        value =
                value / 100.0;

        activeField.setText(
                formatNumber(value)
        );
    }

    // =============================================================
    // BILL BUTTONS
    // =============================================================

    private void setupBillButtons() {

        findViewById(R.id.btnFinalTotal)
                .setOnClickListener(
                        view -> showBillSummary()
                );

        findViewById(R.id.btnGenerateBill)
                .setOnClickListener(
                        view -> showBillSummary()
                );
    }

    // =============================================================
    // BILL PREVIEW
    // =============================================================

    private void showBillSummary() {

        if (billItems.isEmpty()) {

            Toast.makeText(
                    this,
                    "Add at least one item",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        StringBuilder bill =
                new StringBuilder();

        bill.append(
                "SHREE LAXMI SALES\n"
        );

        bill.append(
                "9811393412\n\n"
        );

        bill.append(
                "Invoice: "
        )
                .append(
                        invoicePreview.getText()
                )
                .append("\n");

        bill.append(
                new SimpleDateFormat(
                        "dd MMM yyyy (hh:mm a)",
                        Locale.getDefault()
                ).format(new Date())
        );

        bill.append("\n\n");

        for (
                int i = 0;
                i < billItems.size();
                i++
        ) {

            BillItem item =
                    billItems.get(i);

            bill.append(
                    i + 1
            )
                    .append(". ")
                    .append(item.name)
                    .append("\n");

            bill.append("   ")
                    .append(
                            formatNumber(
                                    item.quantity
                            )
                    )
                    .append(" × ₹")
                    .append(
                            formatNumber(
                                    item.rate
                            )
                    )
                    .append(" = ₹")
                    .append(
                            formatMoney(
                                    item.total
                            )
                    )
                    .append("\n");
        }

        bill.append(
                "\n--------------------\n"
        );

        bill.append(
                "TOTAL: ₹"
        )
                .append(
                        formatMoney(
                                grandTotal
                        )
                );

        new android.app.AlertDialog.Builder(this)
                .setTitle("Bill Preview")
                .setMessage(bill.toString())
                .setPositiveButton(
                        "OK",
                        null
                )
                .show();
    }

    // =============================================================
    // TOTAL
    // =============================================================

    private void updateTotal() {

        totalText.setText(
                "₹"
                        + formatMoney(
                        grandTotal
                )
        );
    }

    // =============================================================
    // INVOICE NUMBER
    // =============================================================

    private void setupInvoiceNumber() {

        String month =
                new SimpleDateFormat(
                        "MMM",
                        Locale.ENGLISH
                )
                        .format(new Date())
                        .toUpperCase(
                                Locale.ENGLISH
                        );

        String savedMonth =
                preferences.getString(
                        LAST_MONTH,
                        ""
                );

        int number;

        if (!month.equals(savedMonth)) {

            number = 1;

            preferences.edit()
                    .putString(
                            LAST_MONTH,
                            month
                    )
                    .putInt(
                            LAST_NUMBER,
                            number
                    )
                    .apply();

        } else {

            number =
                    preferences.getInt(
                            LAST_NUMBER,
                            1
                    );
        }

        invoicePreview.setText(
                String.format(
                        Locale.ENGLISH,
                        "%s %02d",
                        month,
                        number
                )
        );
    }

    // =============================================================
    // HELPERS
    // =============================================================

    private double getActiveFieldValue() {

        if (activeField == null) {

            return 0.0;
        }

        return getNumber(activeField);
    }

    private double getNumber(
            EditText field
    ) {

        try {

            return Double.parseDouble(
                    field.getText()
                            .toString()
                            .trim()
            );

        } catch (Exception e) {

            return 0.0;
        }
    }

    private String formatNumber(
            double value
    ) {

        if (value == (long) value) {

            return String.format(
                    Locale.ENGLISH,
                    "%d",
                    (long) value
            );
        }

        return String.format(
                Locale.ENGLISH,
                "%.2f",
                value
        );
    }

    private String formatMoney(
            double value
    ) {

        return String.format(
                Locale.ENGLISH,
                "%.2f",
                value
        );
    }

    // =============================================================
    // BILL ITEM
    // =============================================================

    private static class BillItem {

        String name;
        double quantity;
        double rate;
        double total;

        BillItem(
                String name,
                double quantity,
                double rate,
                double total
        ) {

            this.name = name;
            this.quantity = quantity;
            this.rate = rate;
            this.total = total;
        }
    }
}
