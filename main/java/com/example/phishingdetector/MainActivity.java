package com.example.phishingdetector;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    EditText url;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        url = findViewById(R.id.url);
        button = findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String urlString = url.getText().toString().trim();

                if (urlString.isEmpty()) {
                    showAlert("ERROR", "Please enter a URL");
                    return;
                }

                String title = "STATUS";
                String safeMessage = urlString + " is SAFE";
                String phishingMessage = urlString + " is PHISHING";

                int phishingCriteriaCount = 0;

                if (!isValidUrlFormat(urlString) || !isValidDomainName(urlString)) {
                    phishingCriteriaCount++;
                }

                if (!urlString.contains("www")) {
                    phishingCriteriaCount++;
                }
                if (urlString.length() > 75) {
                    phishingCriteriaCount++;
                }

                if (countOccurrences(urlString, '-') != 1) {
                    phishingCriteriaCount++;
                }
                if (countOccurrences(urlString, ':') != 1) {
                    phishingCriteriaCount++;
                }
                if (urlString.contains("http")) {
                    phishingCriteriaCount++;
                }
                if (urlString.contains("@")) {
                    phishingCriteriaCount++;
                }
                if (countOccurrences(urlString, '.') >= 4) {
                    phishingCriteriaCount++;
                }
                if (countOccurrences(urlString, "//") >= 2) {
                    phishingCriteriaCount++;
                }
                if (countOccurrences(urlString, "http") > 1) {
                    phishingCriteriaCount++;
                }

                if (phishingCriteriaCount < 3) {
                    showAlert(title, safeMessage);
                    openUrlInBrowser(urlString); // Open URL in browser
                } else {
                    showAlert(title, phishingMessage);
                }
            }
        });
    }

    private void openUrlInBrowser(String urlString) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlString));
        startActivity(intent);
    }

    private boolean isValidUrlFormat(String urlString) {
        String urlPattern = "^(https?|ftp)://[\\w.-]+(:\\d+)?(/([\\w/_.]*)?)?$";
        Pattern pattern = Pattern.compile(urlPattern);
        Matcher matcher = pattern.matcher(urlString);
        return matcher.matches();
    }

    private boolean isValidDomainName(String urlString) {
        String domainPattern = "^(https?|ftp)://([a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,6}(:\\d+)?(/.*)?$";
        Pattern pattern = Pattern.compile(domainPattern);
        Matcher matcher = pattern.matcher(urlString);
        return matcher.matches();
    }

    private int countOccurrences(String input, char character) {
        int count = 0;
        for (int i = 0; i < input.length(); i++) {
            if (input.charAt(i) == character) {
                count++;
            }
        }
        return count;
    }

    private int countOccurrences(String input, String substring) {
        int count = 0;
        int index = 0;
        while ((index = input.indexOf(substring, index)) != -1) {
            count++;
            index += substring.length();
        }
        return count;
    }

    private void showAlert(String title, String message) {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
    }
}
