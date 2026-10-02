package com.shreelaxmisales.app;

import android.app.Activity;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.text.InputType;
import android.view.View;
import android.widget.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {

    private EditText itemName;
    private EditText quantity;
    private EditText rate;

    private TextView itemTotal;
    private TextView totalText;
    private TextView invoicePreview;

    private LinearLayout itemList;

    private final ArrayList<BillItem> items = new ArrayList<>();

    private double currentTotal = 0;

    // Calculator
    private double calculatorValue = 0;
    private String calculatorOperator = "";
    private boolean newCalculatorValue = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        connectViews();
        setupCalculator();
        setupBilling();

        updateItemTotal();
        updateInvoicePreview();
    }

    private void connectViews() {

        itemName = findViewById(R.id.itemName);
        quantity = findViewById(R.id.quantity);
        rate = findViewById(R.id.rate);

        itemTotal = findViewById(R.id.itemTotal);
        totalText = findViewById(R.id.totalText);
        invoicePreview = findViewById(R.id.invoicePreview);

        itemList = findViewById(R.id.itemList);
    }

    // --------------------------------------------------
    // BILLING
    // --------------------------------------------------

    private void setupBilling() {

        quantity.addTextChangedListener(
                new SimpleTextWatcher() {
                    @Override
                    public void afterTextChanged(
                            android.text.Editable s) {
                        updateItemTotal();
                    }
                }
        );

        rate.addTextChangedListener(
                new SimpleTextWatcher() {
                    @Override
                    public void afterTextChanged(
                            android.text.Editable s) {
                        updateItemTotal();
                    }
                }
        );

        findViewById(R.id.nextItemButton)
                .setOnClickListener(v -> addCurrentItem());

        findViewById(R.id.finalTotalButton)
                .setOnClickListener(v -> showFinalTotal());

        findViewById(R.id.generateBillButton)
                .setOnClickListener(v -> generateBill());
    }

    private void updateItemTotal() {

        double q = getNumber(quantity);
        double r = getNumber(rate);

        double total = q * r;

        itemTotal.setText(
                "₹" + formatMoney(total)
        );
    }

    private void addCurrentItem() {

        double q = getNumber(quantity);
        double r = getNumber(rate);

        if (q <= 0 || r < 0) {

            Toast.makeText(
                    this,
                    "Enter quantity and rate",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String name = itemName.getText()
                .toString()
                .trim();

        if (name.isEmpty()) {
            name = "Item " + (items.size() + 1);
        }

        double total = q * r;

        BillItem item =
                new BillItem(
                        name,
                        q,
                        r,
                        total
                );

        items.add(item);

        currentTotal += total;

        addItemRow(item);

        updateTotal();

        clearCurrentItem();
    }

    private void addItemRow(BillItem item) {

        LinearLayout row =
                new LinearLayout(this);

        row.setOrientation(
                LinearLayout.HORIZONTAL
        );

        row.setGravity(
                android.view.Gravity.CENTER_VERTICAL
        );

        row.setPadding(
                16,
                12,
                16,
                12
        );

        row.setBackgroundColor(
                Color.rgb(21, 29, 48)
        );

        LinearLayout details =
                new LinearLayout(this);

        details.setOrientation(
                LinearLayout.VERTICAL
        );

        TextView name =
                new TextView(this);

        name.setText(item.name);

        name.setTextColor(
                Color.WHITE
        );

        name.setTextSize(16);

        TextView calculation =
                new TextView(this);

        calculation.setText(
                formatQuantity(item.quantity)
                        + " × ₹"
                        + formatMoney(item.rate)
                        + " = ₹"
                        + formatMoney(item.total)
        );

        calculation.setTextColor(
                Color.rgb(120, 150, 185)
        );

        calculation.setTextSize(13);

        details.addView(name);
        details.addView(calculation);

        TextView total =
                new TextView(this);

        total.setText(
                "₹" + formatMoney(item.total)
        );

        total.setTextColor(
                Color.rgb(83, 183, 255)
        );

        total.setTextSize(17);

        total.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        row.addView(
                details,
                new LinearLayout.LayoutParams(
                        0,
                        -2,
                        1
                )
        );

        row.addView(
                total,
                new LinearLayout.LayoutParams(
                        -2,
                        -2
                )
        );

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                );

        params.setMargins(
                0,
                6,
                0,
                6
        );

        itemList.addView(
                row,
                params
        );
    }

    private void clearCurrentItem() {

        itemName.setText("");
        quantity.setText("");
        rate.setText("");

        itemTotal.setText("₹0");

        quantity.requestFocus();
    }

    private void updateTotal() {

        totalText.setText(
                "₹" + formatMoney(currentTotal)
        );
    }

    private void showFinalTotal() {

        if (items.isEmpty()) {

            Toast.makeText(
                    this,
                    "Add at least one item",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String invoice =
                getCurrentInvoiceNumber();

        new android.app.AlertDialog.Builder(this)
                .setTitle("FINAL TOTAL")
                .setMessage(
                        "Invoice: "
                                + invoice
                                + "\n\n"
                                + "Items: "
                                + items.size()
                                + "\n\n"
                                + "GRAND TOTAL\n₹"
                                + formatMoney(currentTotal)
                )
                .setPositiveButton(
                        "OK",
                        null
                )
                .show();
    }

    // --------------------------------------------------
    // GENERATE BILL
    // --------------------------------------------------

    private void generateBill() {

        if (!quantity.getText()
                .toString()
                .trim()
                .isEmpty()) {

            addCurrentItem();
        }

        if (items.isEmpty()) {

            Toast.makeText(
                    this,
                    "Add at least one item",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String invoice =
                getNextInvoiceNumber();

        String date =
                new SimpleDateFormat(
                        "dd MMM yyyy (hh:mm a)",
                        Locale.ENGLISH
                ).format(
                        new Date()
                );

        StringBuilder bill =
                new StringBuilder();

        bill.append(
                "SHREE LAXMI SALES\n\n"
        );

        bill.append(
                "Invoice: "
        );

        bill.append(invoice);

        bill.append("\n");

        bill.append(
                "Date: "
        );

        bill.append(date);

        bill.append(
                "\nMobile: 9811393412\n"
        );

        bill.append(
                "\n--------------------------\n"
        );

        for (int i = 0; i < items.size(); i++) {

            BillItem item =
                    items.get(i);

            bill.append(
                    (i + 1)
            );

            bill.append(
                    ". "
            );

            bill.append(
                    item.name
            );

            bill.append(
                    "\n   "
            );

            bill.append(
                    formatQuantity(item.quantity)
            );

            bill.append(
                    " × ₹"
            );

            bill.append(
                    formatMoney(item.rate)
            );

            bill.append(
                    " = ₹"
            );

            bill.append(
                    formatMoney(item.total)
            );

            bill.append("\n");
        }

        bill.append(
                "\n--------------------------\n"
        );

        bill.append(
                "GRAND TOTAL: ₹"
        );

        bill.append(
                formatMoney(currentTotal)
        );

        new android.app.AlertDialog.Builder(this)
                .setTitle("BILL GENERATED")
                .setMessage(bill.toString())
                .setPositiveButton(
                        "DONE",
                        null
                )
                .show();
    }

    // --------------------------------------------------
    // CALCULATOR
    // --------------------------------------------------

    private void setupCalculator() {

        setNumberButton(
                R.id.zeroButton,
                "0"
        );

        setNumberButton(
                R.id.oneButton,
                "1"
        );

        setNumberButton(
                R.id.twoButton,
                "2"
        );

        setNumberButton(
                R.id.threeButton,
                "3"
        );

        setNumberButton(
                R.id.fourButton,
                "4"
        );

        setNumberButton(
                R.id.fiveButton,
                "5"
        );

        setNumberButton(
                R.id.sixButton,
                "6"
        );

        setNumberButton(
                R.id.sevenButton,
                "7"
        );

        setNumberButton(
                R.id.eightButton,
                "8"
        );

        setNumberButton(
                R.id.nineButton,
                "9"
        );

        setNumberButton(
                R.id.doubleZeroButton,
                "00"
        );

        findViewById(R.id.clearButton)
                .setOnClickListener(
                        v -> clearCalculator()
                );

        findViewById(R.id.backspaceButton)
                .setOnClickListener(
                        v -> backspaceCalculator()
                );

        findViewById(R.id.percentButton)
                .setOnClickListener(
                        v -> percentCalculator()
                );

        findViewById(R.id.signButton)
                .setOnClickListener(
                        v -> signCalculator()
                );

        findViewById(R.id.plusButton)
                .setOnClickListener(
                        v -> calculatorOperator("+")
                );

        findViewById(R.id.minusButton)
                .setOnClickListener(
                        v -> calculatorOperator("-")
                );

        findViewById(R.id.multiplyButton)
                .setOnClickListener(
                        v -> calculatorOperator("*")
                );

        findViewById(R.id.divideButton)
                .setOnClickListener(
                        v -> calculatorOperator("/")
                );

        findViewById(R.id.equalButton)
                .setOnClickListener(
                        v -> calculatorEquals()
                );
    }

    private void setNumberButton(
            int id,
            String number
    ) {

        findViewById(id)
                .setOnClickListener(
                        v -> calculatorNumber(number)
                );
    }

    private void calculatorNumber(
            String number
    ) {

        EditText target =
                quantity.hasFocus()
                        ? quantity
                        : rate;

        String current =
                target.getText()
                        .toString();

        if (current.equals("0")) {
            current = "";
        }

        target.setText(
                current + number
        );

        target.setSelection(
                target.length()
        );

        updateItemTotal();
    }

    private void calculatorOperator(
            String operator
    ) {

        double current =
                getCalculatorInput();

        if (!calculatorOperator.isEmpty()) {
            calculateOperation(current);
        } else {
            calculatorValue = current;
        }

        calculatorOperator = operator;

        newCalculatorValue = true;
    }

    private void calculatorEquals() {

        double current =
                getCalculatorInput();

        if (!calculatorOperator.isEmpty()) {

            calculateOperation(current);

            setCalculatorInput(
                    calculatorValue
            );

            calculatorOperator = "";

            newCalculatorValue = true;
        }
    }

    private void calculateOperation(
            double current
    ) {

        switch (calculatorOperator) {

            case "+":
                calculatorValue += current;
                break;

            case "-":
                calculatorValue -= current;
                break;

            case "*":
                calculatorValue *= current;
                break;

            case "/":

                if (current != 0) {
                    calculatorValue /= current;
                }

                break;
        }
    }

    private double getCalculatorInput() {

        EditText target =
                quantity.hasFocus()
                        ? quantity
                        : rate;

        return getNumber(target);
    }

    private void setCalculatorInput(
            double value
    ) {

        EditText target =
                quantity.hasFocus()
                        ? quantity
                        : rate;

        target.setText(
                formatMoney(value)
        );

        target.setSelection(
                target.length()
        );

        updateItemTotal();
    }

    private void clearCalculator() {

        quantity.setText("");
        rate.setText("");

        calculatorValue = 0;
        calculatorOperator = "";

        updateItemTotal();
    }

    private void backspaceCalculator() {

        EditText target =
                quantity.hasFocus()
                        ? quantity
                        : rate;

        String text =
                target.getText()
                        .toString();

        if (!text.isEmpty()) {

            target.setText(
                    text.substring(
                            0,
                            text.length() - 1
                    )
            );

            target.setSelection(
                    target.length()
            );
        }

        updateItemTotal();
    }

    private void percentCalculator() {

        EditText target =
                quantity.hasFocus()
                        ? quantity
                        : rate;

        double value =
                getNumber(target);

        target.setText(
                formatMoney(value / 100)
        );

        target.setSelection(
                target.length()
        );

        updateItemTotal();
    }

    private void signCalculator() {

        EditText target =
                quantity.hasFocus()
                        ? quantity
                        : rate;

        double value =
                getNumber(target);

        target.setText(
                formatMoney(-value)
        );

        target.setSelection(
                target.length()
        );

        updateItemTotal();
    }

    // --------------------------------------------------
    // INVOICE NUMBER
    // --------------------------------------------------

    private String getCurrentInvoiceNumber() {

        String month =
                new SimpleDateFormat(
                        "MMM",
                        Locale.ENGLISH
                ).format(
                        new Date()
                ).toUpperCase();

        SharedPreferences prefs =
                getSharedPreferences(
                        "invoice_data",
                        MODE_PRIVATE
                );

        String savedMonth =
                prefs.getString(
                        "month",
                        ""
                );

        int number =
                prefs.getInt(
                        "number",
                        0
                );

        if (!month.equals(savedMonth)) {
            number = 0;
        }

        return String.format(
                Locale.ENGLISH,
                "%s %02d",
                month,
                number + 1
        );
    }

    private String getNextInvoiceNumber() {

        String month =
                new SimpleDateFormat(
                        "MMM",
                        Locale.ENGLISH
                ).format(
                        new Date()
                ).toUpperCase();

        SharedPreferences prefs =
                getSharedPreferences(
                        "invoice_data",
                        MODE_PRIVATE
                );

        String savedMonth =
                prefs.getString(
                        "month",
                        ""
                );

        int number =
                prefs.getInt(
                        "number",
                        0
                );

        if (!month.equals(savedMonth)) {
            number = 1;
        } else {
            number++;
        }

        prefs.edit()
                .putString(
                        "month",
                        month
                )
                .putInt(
                        "number",
                        number
                )
                .apply();

        return String.format(
                Locale.ENGLISH,
                "%s %02d",
                month,
                number
        );
    }

    private void updateInvoicePreview() {

        invoicePreview.setText(
                getCurrentInvoiceNumber()
        );
    }

    // --------------------------------------------------
    // HELPERS
    // --------------------------------------------------

    private double getNumber(
            EditText editText
    ) {

        try {

            String value =
                    editText.getText()
                            .toString()
                            .trim();

            if (value.isEmpty()) {
                return 0;
            }

            return Double.parseDouble(value);

        } catch (Exception e) {

            return 0;
        }
    }

    private String formatMoney(
            double value
    ) {

        if (value ==
                (long) value) {

            return String.valueOf(
                    (long) value
            );
        }

        return String.format(
                Locale.ENGLISH,
                "%.2f",
                value
        );
    }

    private String formatQuantity(
            double value
    ) {

        if (value ==
                (long) value) {

            return String.valueOf(
                    (long) value
            );
        }

        return String.format(
                Locale.ENGLISH,
                "%.2f",
                value
        );
    }

    // --------------------------------------------------
    // BILL ITEM
    // --------------------------------------------------

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

    // --------------------------------------------------
    // SIMPLE TEXT WATCHER
    // --------------------------------------------------

    private abstract static class SimpleTextWatcher
            implements android.text.TextWatcher {

        @Override
        public void beforeTextChanged(
                CharSequence s,
                int start,
                int count,
                int after) {
        }

        @Override
        public void onTextChanged(
                CharSequence s,
                int start,
                int before,
                int count) {
        }
    }
}
