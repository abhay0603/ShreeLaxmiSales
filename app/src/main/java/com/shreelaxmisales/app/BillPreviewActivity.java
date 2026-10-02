package com.shreelaxmisales.app;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class BillPreviewActivity extends AppCompatActivity {

    private LinearLayout tableContainer;

    private TextView billInvoice;
    private TextView billDateTime;
    private TextView billGrandTotal;

    private ArrayList<String> itemNames;
    private ArrayList<String> itemQuantities;
    private ArrayList<String> itemRates;
    private ArrayList<String> itemAmounts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_bill_preview);

        // ---------------------------------------------------------
        // FIND VIEWS
        // ---------------------------------------------------------

        tableContainer =
                findViewById(R.id.tableContainer);

        billInvoice =
                findViewById(R.id.billInvoice);

        billDateTime =
                findViewById(R.id.billDateTime);

        billGrandTotal =
                findViewById(R.id.billGrandTotal);

        Button btnNewBill =
                findViewById(R.id.btnNewBill);

        Button btnSendWhatsApp =
                findViewById(R.id.btnSendWhatsApp);

        // ---------------------------------------------------------
        // RECEIVE BILL DATA
        // ---------------------------------------------------------

        itemNames =
                getIntent().getStringArrayListExtra(
                        "item_names"
                );

        itemQuantities =
                getIntent().getStringArrayListExtra(
                        "item_quantities"
                );

        itemRates =
                getIntent().getStringArrayListExtra(
                        "item_rates"
                );

        itemAmounts =
                getIntent().getStringArrayListExtra(
                        "item_amounts"
                );

        String invoice =
                getIntent().getStringExtra(
                        "invoice_number"
                );

        String dateTime =
                getIntent().getStringExtra(
                        "date_time"
                );

        String grandTotal =
                getIntent().getStringExtra(
                        "grand_total"
                );

        // ---------------------------------------------------------
        // DISPLAY BILL INFORMATION
        // ---------------------------------------------------------

        if (invoice != null) {

            billInvoice.setText(invoice);
        }

        if (dateTime != null) {

            billDateTime.setText(dateTime);
        }

        if (grandTotal != null) {

            billGrandTotal.setText(
                    "₹" + grandTotal
            );
        }

        // ---------------------------------------------------------
        // CREATE TABLE ROWS
        // ---------------------------------------------------------

        createBillRows();

        // ---------------------------------------------------------
        // NEW BILL
        // ---------------------------------------------------------

        btnNewBill.setOnClickListener(
                view -> {

                    /*
                     * Returning to MainActivity starts a fresh
                     * billing screen.
                     */
                    finish();
                }
        );

        // ---------------------------------------------------------
        // WHATSAPP
        // ---------------------------------------------------------

        btnSendWhatsApp.setOnClickListener(
                view -> {

                    /*
                     * WhatsApp image sharing will be implemented
                     * in the next step.
                     */
                }
        );
    }

    // =============================================================
    // CREATE BILL TABLE
    // =============================================================

    private void createBillRows() {

        if (itemNames == null
                || itemQuantities == null
                || itemRates == null
                || itemAmounts == null) {

            return;
        }

        int count =
                itemNames.size();

        for (int i = 0; i < count; i++) {

            addBillRow(
                    i + 1,
                    itemNames.get(i),
                    itemQuantities.get(i),
                    itemRates.get(i),
                    itemAmounts.get(i)
            );
        }
    }

    // =============================================================
    // ADD ONE ROW
    // =============================================================

    private void addBillRow(
            int serialNumber,
            String itemName,
            String quantity,
            String rate,
            String amount
    ) {

        LinearLayout row =
                new LinearLayout(this);

        row.setOrientation(
                LinearLayout.HORIZONTAL
        );

        row.setGravity(
                Gravity.CENTER_VERTICAL
        );

        row.setPadding(
                dp(8),
                dp(12),
                dp(8),
                dp(12)
        );

        /*
         * Alternate row background makes the table easier
         * to read while keeping the dark premium design.
         */
        if (serialNumber % 2 == 0) {

            row.setBackgroundColor(
                    getColor(
                            R.color.sls_surface_2
                    )
            );

        } else {

            row.setBackgroundColor(
                    getColor(
                            R.color.sls_surface
                    )
            );
        }

        // ---------------------------------------------------------
        // S.NO.
        // ---------------------------------------------------------

        TextView serial =
                createCell(
                        String.valueOf(
                                serialNumber
                        ),
                        38,
                        Gravity.CENTER
                );

        row.addView(serial);

        // ---------------------------------------------------------
        // ITEM
        // ---------------------------------------------------------

        TextView name =
                createItemCell(
                        itemName
                );

        row.addView(name);

        // ---------------------------------------------------------
        // QTY
        // ---------------------------------------------------------

        TextView qty =
                createCell(
                        quantity,
                        48,
                        Gravity.CENTER
                );

        row.addView(qty);

        // ---------------------------------------------------------
        // RATE
        // ---------------------------------------------------------

        TextView rateView =
                createCell(
                        "₹" + rate,
                        70,
                        Gravity.END
                );

        row.addView(rateView);

        // ---------------------------------------------------------
        // AMOUNT
        // ---------------------------------------------------------

        TextView amountView =
                createCell(
                        "₹" + amount,
                        82,
                        Gravity.END
                );

        amountView.setTextColor(
                getColor(
                        R.color.sls_gold_light
                )
        );

        amountView.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        row.addView(amountView);

        // ---------------------------------------------------------
        // ADD ROW
        // ---------------------------------------------------------

        tableContainer.addView(row);

        /*
         * Horizontal separator.
         */
        ViewLine separator =
                new ViewLine(this);

        tableContainer.addView(separator);
    }

    // =============================================================
    // NORMAL TABLE CELL
    // =============================================================

    private TextView createCell(
            String text,
            int widthDp,
            int gravity
    ) {

        TextView textView =
                new TextView(this);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        dp(widthDp),
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        textView.setLayoutParams(params);

        textView.setGravity(gravity);

        textView.setText(text);

        textView.setTextColor(
                getColor(
                        R.color.sls_white
                )
        );

        textView.setTextSize(12);

        return textView;
    }

    // =============================================================
    // ITEM CELL
    // =============================================================

    private TextView createItemCell(
            String text
    ) {

        TextView textView =
                new TextView(this);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                );

        textView.setLayoutParams(params);

        textView.setText(text);

        textView.setTextColor(
                getColor(
                        R.color.sls_white
                )
        );

        textView.setTextSize(12);

        textView.setGravity(
                Gravity.CENTER_VERTICAL
        );

        /*
         * Prevent very long item names from making the table
         * unusable.
         */
        textView.setMaxLines(2);

        return textView;
    }

    // =============================================================
    // DP HELPER
    // =============================================================

    private int dp(int value) {

        return Math.round(
                value
                        * getResources()
                        .getDisplayMetrics()
                        .density
        );
    }

    // =============================================================
    // SIMPLE DIVIDER VIEW
    // =============================================================

    private static class ViewLine
            extends android.view.View {

        ViewLine(
                android.content.Context context
        ) {

            super(context);

            setBackgroundColor(
                    android.graphics.Color
                            .parseColor(
                                    "#25334D"
                            )
            );

            setLayoutParams(
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            1
                    )
            );
        }
    }
}
