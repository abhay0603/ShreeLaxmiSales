package com.shreelaxmisales.app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class BillPreviewActivity extends AppCompatActivity {

    private LinearLayout billCard;
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

        billCard = findViewById(R.id.billCard);

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
        // CREATE TABLE
        // ---------------------------------------------------------

        createBillRows();

        // ---------------------------------------------------------
        // SEND ON WHATSAPP
        // ---------------------------------------------------------

        btnSendWhatsApp.setOnClickListener(
                view -> shareBillImage()
        );

        // ---------------------------------------------------------
        // NEW BILL
        // ---------------------------------------------------------

        btnNewBill.setOnClickListener(
                view -> {

                    Intent intent =
                            new Intent(
                                    BillPreviewActivity.this,
                                    MainActivity.class
                            );

                    intent.addFlags(
                            Intent.FLAG_ACTIVITY_CLEAR_TOP
                    );

                    startActivity(intent);

                    finish();
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
    // ADD TABLE ROW
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

        // S.NO.

        TextView serial =
                createCell(
                        String.valueOf(
                                serialNumber
                        ),
                        38,
                        Gravity.CENTER
                );

        row.addView(serial);

        // ITEM

        TextView name =
                createItemCell(
                        itemName
                );

        row.addView(name);

        // QTY

        TextView qty =
                createCell(
                        quantity,
                        48,
                        Gravity.CENTER
                );

        row.addView(qty);

        // RATE

        TextView rateView =
                createCell(
                        "₹" + rate,
                        70,
                        Gravity.END
                );

        row.addView(rateView);

        // AMOUNT

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
                Typeface.BOLD
        );

        row.addView(amountView);

        // ADD ROW

        tableContainer.addView(row);

        View separator =
                new View(this);

        separator.setBackgroundColor(
                Color.parseColor(
                        "#25334D"
                )
        );

        LinearLayout.LayoutParams separatorParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(1)
                );

        separator.setLayoutParams(
                separatorParams
        );

        tableContainer.addView(
                separator
        );
    }

    // =============================================================
    // NORMAL CELL
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

        textView.setMaxLines(2);

        return textView;
    }

    // =============================================================
    // SHARE BILL IMAGE
    // =============================================================

    private void shareBillImage() {

        /*
         * Make sure the bill has been laid out before
         * taking the screenshot.
         */

        billCard.post(
                () -> {

                    try {

                        Bitmap bitmap =
                                Bitmap.createBitmap(
                                        billCard.getWidth(),
                                        billCard.getHeight(),
                                        Bitmap.Config.ARGB_8888
                                );

                        Canvas canvas =
                                new Canvas(bitmap);

                        billCard.draw(canvas);

                        File billsDirectory =
                                new File(
                                        getCacheDir(),
                                        "bills"
                                );

                        if (!billsDirectory.exists()) {

                            boolean created =
                                    billsDirectory.mkdirs();

                            if (!created) {

                                Toast.makeText(
                                        this,
                                        "Unable to create bill file",
                                        Toast.LENGTH_SHORT
                                ).show();

                                return;
                            }
                        }

                        String fileName =
                                "Bill_"
                                        + System.currentTimeMillis()
                                        + ".png";

                        File imageFile =
                                new File(
                                        billsDirectory,
                                        fileName
                                );

                        FileOutputStream outputStream =
                                new FileOutputStream(
                                        imageFile
                                );

                        bitmap.compress(
                                Bitmap.CompressFormat.PNG,
                                100,
                                outputStream
                        );

                        outputStream.flush();

                        outputStream.close();

                        bitmap.recycle();

                        Uri imageUri =
                                FileProvider.getUriForFile(
                                        this,
                                        getPackageName()
                                                + ".fileprovider",
                                        imageFile
                                );

                        Intent shareIntent =
                                new Intent(
                                        Intent.ACTION_SEND
                                );

                        shareIntent.setType(
                                "image/png"
                        );

                        shareIntent.putExtra(
                                Intent.EXTRA_STREAM,
                                imageUri
                        );

                        shareIntent.addFlags(
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );

                        /*
                         * Open the Android share chooser.
                         * WhatsApp will appear if installed.
                         */
                        startActivity(
                                Intent.createChooser(
                                        shareIntent,
                                        "Send Bill"
                                )
                        );

                    } catch (Exception e) {

                        Toast.makeText(
                                this,
                                "Unable to create bill image",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    // =============================================================
    // DP
    // =============================================================

    private int dp(int value) {

        return Math.round(
                value
                        * getResources()
                        .getDisplayMetrics()
                        .density
        );
    }
}
