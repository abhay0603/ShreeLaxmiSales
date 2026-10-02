package com.shreelaxmisales.app;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {

    private LinearLayout itemList;
    private TextView totalText;
    private EditText itemName;
    private EditText quantity;
    private EditText rate;

    private final ArrayList<BillItem> items = new ArrayList<>();

    private double currentTotal = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createInterface();
    }

    private void createInterface() {

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(20, 20, 20, 20);
        root.setBackgroundColor(Color.rgb(10, 16, 28));

        // Header
        TextView title = new TextView(this);
        title.setText("SHREE LAXMI SALES");
        title.setTextColor(Color.WHITE);
        title.setTextSize(23);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);

        root.addView(title,
                new LinearLayout.LayoutParams(
                        -1, 70
                ));

        // Top total bar
        LinearLayout totalBar = new LinearLayout(this);
        totalBar.setGravity(Gravity.CENTER_VERTICAL);
        totalBar.setPadding(20, 10, 10, 10);
        totalBar.setBackgroundColor(Color.rgb(24, 34, 52));

        totalText = new TextView(this);
        totalText.setText("TOTAL  ₹0");
        totalText.setTextColor(Color.WHITE);
        totalText.setTextSize(20);
        totalText.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        Button finalTotal = new Button(this);
        finalTotal.setText("FINAL TOTAL");
        finalTotal.setTextColor(Color.WHITE);
        finalTotal.setBackgroundColor(Color.rgb(30, 120, 255));

        totalBar.addView(totalText,
                new LinearLayout.LayoutParams(0, 70, 1));

        totalBar.addView(finalTotal,
                new LinearLayout.LayoutParams(160, 70));

        root.addView(totalBar);

        // Item name
        itemName = new EditText(this);
        itemName.setHint("Item name (optional)");
        itemName.setTextColor(Color.WHITE);
        itemName.setHintTextColor(Color.GRAY);

        root.addView(itemName,
                new LinearLayout.LayoutParams(-1, 65));

        // Quantity + Rate
        LinearLayout inputRow = new LinearLayout(this);

        quantity = createInput("Quantity");
        rate = createInput("Rate ₹");

        inputRow.addView(quantity,
                new LinearLayout.LayoutParams(0, 70, 1));

        inputRow.addView(rate,
                new LinearLayout.LayoutParams(0, 70, 1));

        root.addView(inputRow);

        // Item total preview
        TextView itemTotal = new TextView(this);
        itemTotal.setText("Item Total  ₹0");
        itemTotal.setTextColor(Color.rgb(80, 190, 255));
        itemTotal.setTextSize(18);
        itemTotal.setPadding(10, 10, 10, 10);

        root.addView(itemTotal);

        // Next item button
        Button nextItem = new Button(this);
        nextItem.setText("NEXT ITEM");
        nextItem.setTextColor(Color.WHITE);
        nextItem.setBackgroundColor(Color.rgb(45, 55, 75));

        root.addView(nextItem,
                new LinearLayout.LayoutParams(-1, 65));

        // Item list
        ScrollView scroll = new ScrollView(this);

        itemList = new LinearLayout(this);
        itemList.setOrientation(LinearLayout.VERTICAL);
        itemList.setPadding(0, 10, 0, 10);

        scroll.addView(itemList);

        root.addView(scroll,
                new LinearLayout.LayoutParams(
                        -1, 0, 1
                ));

        // Generate bill button
        Button generateBill = new Button(this);
        generateBill.setText("GENERATE BILL");
        generateBill.setTextColor(Color.WHITE);
        generateBill.setTextSize(17);
        generateBill.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        generateBill.setBackgroundColor(Color.rgb(210, 145, 40));

        root.addView(generateBill,
                new LinearLayout.LayoutParams(-1, 75));

        // Quantity/rate calculation
        View.OnFocusChangeListener calculationListener =
                (v, hasFocus) -> updatePreview(itemTotal);

        quantity.setOnFocusChangeListener(calculationListener);
        rate.setOnFocusChangeListener(calculationListener);

        nextItem.setOnClickListener(v -> {

            addCurrentItem(itemTotal);

        });

        finalTotal.setOnClickListener(v -> {

            Toast.makeText(
                    this,
                    "Final Total: ₹" + format(currentTotal),
                    Toast.LENGTH_LONG
            ).show();

        });

        generateBill.setOnClickListener(v -> {

            if (!quantity.getText().toString().isEmpty()
                    && !rate.getText().toString().isEmpty()) {

                addCurrentItem(itemTotal);
            }

            Toast.makeText(
                    this,
                    "Bill generated successfully",
                    Toast.LENGTH_LONG
            ).show();

        });

        setContentView(root);
    }

    private EditText createInput(String hint) {

        EditText edit = new EditText(this);

        edit.setHint(hint);
        edit.setHintTextColor(Color.GRAY);
        edit.setTextColor(Color.WHITE);
        edit.setTextSize(18);
        edit.setInputType(
                android.text.InputType.TYPE_CLASS_NUMBER |
                        android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        );

        return edit;
    }

    private void updatePreview(TextView preview) {

        try {

            double q = Double.parseDouble(
                    quantity.getText().toString()
            );

            double r = Double.parseDouble(
                    rate.getText().toString()
            );

            preview.setText(
                    "Item Total  ₹" + format(q * r)
            );

        } catch (Exception e) {

            preview.setText("Item Total  ₹0");

        }
    }

    private void addCurrentItem(TextView preview) {

        String qText = quantity.getText().toString();
        String rText = rate.getText().toString();

        if (qText.isEmpty() || rText.isEmpty()) {

            Toast.makeText(
                    this,
                    "Enter quantity and rate",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        double q = Double.parseDouble(qText);
        double r = Double.parseDouble(rText);

        double total = q * r;

        String name = itemName.getText().toString();

        if (name.isEmpty()) {
            name = "Item " + (items.size() + 1);
        }

        BillItem item = new BillItem(
                name,
                q,
                r,
                total
        );

        items.add(item);

        currentTotal += total;

        addItemRow(item);

        totalText.setText(
                "TOTAL  ₹" + format(currentTotal)
        );

        itemName.setText("");
        quantity.setText("");
        rate.setText("");

        preview.setText("Item Total  ₹0");
    }

    private void addItemRow(BillItem item) {

        LinearLayout row = new LinearLayout(this);

        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(18, 12, 18, 12);
        row.setBackgroundColor(Color.rgb(20, 28, 43));

        TextView name = new TextView(this);

        name.setText(
                item.name +
                        "\n" +
                        item.quantity +
                        " × ₹" +
                        format(item.rate) +
                        " = ₹" +
                        format(item.total)
        );

        name.setTextColor(Color.WHITE);
        name.setTextSize(16);

        row.addView(name);

        itemList.addView(row,
                new LinearLayout.LayoutParams(
                        -1, 90
                ));

        Space space = new Space(this);

        itemList.addView(space,
                new LinearLayout.LayoutParams(
                        1, 8
                ));
    }

    private String format(double value) {

        if (value == (long) value) {

            return String.valueOf((long) value);

        }

        return String.format(
                Locale.US,
                "%.2f",
                value
        );
    }

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
