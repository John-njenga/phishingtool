package com.example.phishingdetector;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    EditText url;
    Button button;
    private static final String TAG = "PhishingDetector";

    // Define the list of correct domain names
    private static final String[] correctDomains = {"gooogle.com", "faceboook.com", "tw1tter.com", "1nstagram.com", "1inkedin.com", "paypa1.com", "goog1e.com"};

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

                if (!isValidUrlFormat(urlString)) {
                    showAlert("ERROR", "Invalid URL format. Please enter a valid URL starting with 'https://'");
                    return;
                }

                String title = "STATUS";
                String safeMessage = urlString + " is SAFE";
                String phishingMessage = urlString + " is PHISHING";

                int phishingCriteriaCount = 0;

                // Check if URL starts with "https://"
                if (!urlString.startsWith("https://")) {
                    phishingCriteriaCount++;
                    Log.d(TAG, "URL does not start with HTTPS.");
                }

                // Check if URL contains only one slash after "https://"
                if (countOccurrences(urlString, '/') != 2) {
                    phishingCriteriaCount++;
                    Log.d(TAG, "URL does not contain exactly one slash after HTTPS.");
                }

                if (!isValidDomainName(urlString)) {
                    phishingCriteriaCount++;
                    Log.d(TAG, "Invalid domain name.");
                }

                if (urlString.length() > 75) {
                    phishingCriteriaCount++;
                    Log.d(TAG, "URL length exceeds 75 characters.");
                }

                if (countOccurrences(urlString, '-') != 1) {
                    phishingCriteriaCount++;
                    Log.d(TAG, "URL does not contain exactly one hyphen.");
                }
                if (countOccurrences(urlString, ':') != 1) {
                    phishingCriteriaCount++;
                    Log.d(TAG, "URL does not contain exactly one colon.");
                }
                if (urlString.contains("@")) {
                    phishingCriteriaCount++;
                    Log.d(TAG, "URL contains '@' symbol.");
                }
                if (countOccurrences(urlString, '.') >= 4) {
                    phishingCriteriaCount++;
                    Log.d(TAG, "URL contains four or more dots.");
                }
                if (countOccurrences(urlString, "//") >= 2) {
                    phishingCriteriaCount++;
                    Log.d(TAG, "URL contains two or more double slashes.");
                }
                if (countOccurrences(urlString, "http") > 1) {
                    phishingCriteriaCount++;
                    Log.d(TAG, "URL contains 'http' more than once.");
                }
                if (containsSuspiciousKeywords(urlString)) {
                    phishingCriteriaCount++;
                    Log.d(TAG, "URL contains suspicious keywords.");
                }

                if (isCommonMisspelledDomain(urlString)) {
                    phishingCriteriaCount++;
                    Log.d(TAG, "URL contains commonly misspelled domain name.");
                }

                // Check if URL contains two or more consecutive dots
                if (urlString.contains("..")) {
                    phishingCriteriaCount++;
                    Log.d(TAG, "URL contains two or more consecutive dots.");
                }

                // Check if the domain name is misspelled
                if (!isCorrectDomain(urlString)) {
                    phishingCriteriaCount++;
                    Log.d(TAG, "URL contains misspelled domain name.");
                }
                // Check if the domain name is misspelled
                if (containsNumberInDomain(urlString)) {
                    phishingCriteriaCount++;
                    Log.d(TAG, "URL contains a number in the domain name.");
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
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlString));
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            showAlert("ERROR", "No application can handle this request. Please install a web browser or check your URL.");
            Log.e(TAG, "ActivityNotFoundException: " + e.getMessage());
        }
    }

    private boolean isValidUrlFormat(String urlString) {
        String urlPattern = "^(https?|ftp)://[\\w.-]+(:\\d+)?(/([\\w/_.]*)?)?$";
        Pattern pattern = Pattern.compile(urlPattern);
        Matcher matcher = pattern.matcher(urlString);
        return matcher.matches();
    }

    private boolean containsNumberInDomain(String urlString) {
        // Check if the domain name contains a number
        Pattern pattern = Pattern.compile("\\d");
        Matcher matcher = pattern.matcher(urlString);
        return matcher.find();
    }

    private boolean isValidDomainName(String urlString) {
        String domainPattern = "^(https?|ftp)://([a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,6}(:\\d+)?(/.*)?$";
        Pattern pattern = Pattern.compile(domainPattern);
        Matcher matcher = pattern.matcher(urlString);
        return matcher.matches();
    }

    private boolean containsSuspiciousKeywords(String urlString) {
        String[] suspiciousKeywords = {"password", "secure", "account", "update", "banking", "verify", "credit card"};
        for (String keyword : suspiciousKeywords) {
            if (urlString.toLowerCase().contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private boolean isCommonMisspelledDomain(String urlString) {
        for (String domain : correctDomains) {
            if (urlString.toLowerCase().contains(domain)) {
                return true;
            }
        }
        return false;
    }

    private boolean isCorrectDomain(String urlString) {
        for (String domain : correctDomains) {
            if (urlString.equalsIgnoreCase("https://" + domain)) {
                return true;
            }
        }
        return false;
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
