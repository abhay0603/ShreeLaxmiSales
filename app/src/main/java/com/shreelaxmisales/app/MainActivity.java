package com.shreelaxmisales.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

    private EditText activeField;

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

        itemName = findViewById(R.id.itemName);
        quantity = findViewById(R.id.quantity);
        rate = findViewById(R.id.rate);

        totalText = findViewById(R.id.totalText);
        invoicePreview = findViewById(R.id.invoicePreview);
        itemCountText = findViewById(R.id.itemCountText);
        lastItemText = findViewById(R.id.lastItemText);

        btnMultiply = findViewById(R.id.btnMultiply);
        btnNextItem = findViewById(R.id.btnNextItem);

        preferences = getSharedPreferences(PREFS, MODE_PRIVATE);

        setupInvoiceNumber();

        setupNumericFields();

        setupCalculator();

        setupMultiplyButton();

        setupNextItem();

        setupOtherButtons();

        focusQuantity();
    }

    // ------------------------------------------------------------
    // NUMERIC FIELD SETUP
    // ------------------------------------------------------------

    private void setupNumericFields() {

        quantity.setShowSoftInputOnFocus(false);
        rate.setShowSoftInputOnFocus(false);

        quantity.setCursorVisible(false);
        rate.setCursorVisible(false);

        quantity.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                activeField = quantity;
            }
        });

        rate.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                activeField = rate;
            }
        });

        quantity.setOnClickListener(v -> {
            activeField = quantity;
            hideKeyboard();
        });

        rate.setOnClickListener(v -> {
            activeField = rate;
            hideKeyboard();
        });

        itemName.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                activeField = null;
            }
        });
    }

    private void focusQuantity() {

        itemName.clearFocus();
        rate.clearFocus();

        quantity.requestFocus();

        activeField = quantity;

        hideKeyboard();

        quantity.setSelection(quantity.length());
    }

    private void focusRate() {

        quantity.clearFocus();

        rate.requestFocus();

        activeField = rate;

        hideKeyboard();

        rate.setSelection(rate.length());
    }

    private void hideKeyboard() {

        View view = getCurrentFocus();

        if (view != null) {

            InputMethodManager imm =
                    (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

            if (imm != null) {
                imm.hideSoftInputFromWindow(
                        view.getWindowToken(),
                        0
                );
            }
        }
    }

    // ------------------------------------------------------------
    // MULTIPLY BUTTON
    // ------------------------------------------------------------

    private void setupMultiplyButton() {

        btnMultiply.setOnClickListener(v -> {

            String quantityValue = quantity.getText().toString().trim();

            if (quantityValue.isEmpty()) {
                quantity.setText("0");
            }

            focusRate();
        });
    }

    // ------------------------------------------------------------
    // NEXT ITEM
    // ------------------------------------------------------------

    private void setupNextItem() {

        btnNextItem.setOnClickListener(v -> addCurrentItem());
    }

    private void addCurrentItem() {

        double qty = getNumber(quantity);
        double price = getNumber(rate);

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

        double itemTotal = qty * price;

        String name = itemName.getText()
                .toString()
                .trim();

        if (name.isEmpty()) {
            name = "Item " + (billItems.size() + 1);
        }

        billItems.add(
                new BillItem(
                        name,
                        qty,
                        price,
                        itemTotal
                )
        );

        grandTotal += itemTotal;

        updateTotal();

        itemCountText.setText(
                billItems.size() + " item"
                        + (billItems.size() == 1 ? "" : "s")
                        + " added"
        );

        lastItemText.setText(
                formatNumber(qty)
                        + " × ₹"
                        + formatNumber(price)
                        + " = ₹"
                        + formatMoney(itemTotal)
        );

        clearCurrentItem();

        focusQuantity();
    }

    private void clearCurrentItem() {

        itemName.setText("");

        quantity.setText("0");

        rate.setText("0");

        calculatorOperator = "";

        calculatorValue = 0.0;

        waitingForSecondValue = false;
    }

    // ------------------------------------------------------------
    // CALCULATOR
    // ------------------------------------------------------------

    private void setupCalculator() {

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

        setNumberButton(R.id.btn00, "00");

        findViewById(R.id.btnDecimal)
                .setOnClickListener(v -> appendDecimal());

        findViewById(R.id.btnSign)
                .setOnClickListener(v -> toggleSign());

        findViewById(R.id.btnBackspace)
                .setOnClickListener(v -> backspace());

        findViewById(R.id.btnClear)
                .setOnClickListener(v -> clearCalculator());

        findViewById(R.id.btnPercent)
                .setOnClickListener(v -> percentage());

        findViewById(R.id.btnPlus)
                .setOnClickListener(v -> chooseOperator("+"));

        findViewById(R.id.btnMinus)
                .setOnClickListener(v -> chooseOperator("-"));

        findViewById(R.id.btnDivide)
                .setOnClickListener(v -> chooseOperator("/"));

        findViewById(R.id.btnMultiply)
                .setOnClickListener(v -> {

                    calculatorOperator = "*";

                    calculatorValue =
                            getActiveFieldValue();

                    waitingForSecondValue = true;

                    focusRate();
                });
    }

    private void setNumberButton(int id, String value) {

        findViewById(id)
                .setOnClickListener(v -> appendNumber(value));
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
                    activeField.getText().toString();

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
                activeField.getText().toString();

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
                activeField.getText().toString();

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
                activeField.getText().toString();

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

    private void clearCalculator() {

        if (activeField == null) {
            focusQuantity();
        }

        activeField.setText("0");

        calculatorOperator = "";

        calculatorValue = 0.0;

        waitingForSecondValue = false;
    }

    private void percentage() {

        if (activeField == null) {
            focusQuantity();
        }

        double value =
                getActiveFieldValue();

        value = value / 100.0;

        activeField.setText(
                formatNumber(value)
        );
    }

    private void chooseOperator(String operator) {

        calculatorValue =
                getActiveFieldValue();

        calculatorOperator = operator;

        waitingForSecondValue = true;
    }

    private double getActiveFieldValue() {

        if (activeField == null) {
            return 0.0;
        }

        return getNumber(activeField);
    }

    // ------------------------------------------------------------
    // OTHER BUTTONS
    // ------------------------------------------------------------

    private void setupOtherButtons() {

        findViewById(R.id.btnFinalTotal)
                .setOnClickListener(v -> showBillSummary());

        findViewById(R.id.btnGenerateBill)
                .setOnClickListener(v -> showBillSummary());
    }

    // ------------------------------------------------------------
    // BILL SUMMARY
    // ------------------------------------------------------------

    private void showBillSummary() {

        if (billItems.isEmpty()) {

            Toast.makeText(
                    this,
                    "Add at least one item",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        StringBuilder bill = new StringBuilder();

        bill.append("SHREE LAXMI SALES\n");
        bill.append("9811393412\n\n");

        bill.append("Invoice: ")
                .append(invoicePreview.getText())
                .append("\n");

        bill.append(
                new SimpleDateFormat(
                        "dd MMM yyyy (hh:mm a)",
                        Locale.getDefault()
                ).format(new Date())
        );

        bill.append("\n\n");

        for (int i = 0; i < billItems.size(); i++) {

            BillItem item =
                    billItems.get(i);

            bill.append(i + 1)
                    .append(". ")
                    .append(item.name)
                    .append("\n");

            bill.append("   ")
                    .append(formatNumber(item.quantity))
                    .append(" × ₹")
                    .append(formatNumber(item.rate))
                    .append(" = ₹")
                    .append(formatMoney(item.total))
                    .append("\n");
        }

        bill.append("\n--------------------\n");

        bill.append("TOTAL: ₹")
                .append(formatMoney(grandTotal));

        new android.app.AlertDialog.Builder(this)
                .setTitle("Bill Preview")
                .setMessage(bill.toString())
                .setPositiveButton("OK", null)
                .show();
    }

    // ------------------------------------------------------------
    // TOTAL
    // ------------------------------------------------------------

    private void updateTotal() {

        totalText.setText(
                "₹" + formatMoney(grandTotal)
        );
    }

    // ------------------------------------------------------------
    // INVOICE NUMBER
    // ------------------------------------------------------------

    private void setupInvoiceNumber() {

        String month =
                new SimpleDateFormat(
                        "MMM",
                        Locale.ENGLISH
                )
                        .format(new Date())
                        .toUpperCase(Locale.ENGLISH);

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

    // ------------------------------------------------------------
    // HELPERS
    // ------------------------------------------------------------

    private double getNumber(EditText field) {

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

    private String formatNumber(double value) {

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

    private String formatMoney(double value) {

        return String.format(
                Locale.ENGLISH,
                "%.2f",
                value
        );
    }

    // ------------------------------------------------------------
    // BILL ITEM
    // ------------------------------------------------------------

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
