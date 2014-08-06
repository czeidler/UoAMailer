/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.mailer;

import android.app.AlertDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import rx.Observer;
import rx.Subscription;
import rx.android.concurrency.AndroidSchedulers;
import rx.concurrency.Schedulers;


import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;


public class SendingDialog extends AlertDialog {
    private TextView statusView;
    private Subscription currentSubscription = null;
    private String upi;
    private String password;
    private Button okButton;
    private Button cancelButton;
    private URL serverAddress;

    private GroupMembers groupMembers;
    private List<Uri> attachments;

    public SendingDialog(Context context, String upi, String password) {
        super(context);

        this.upi = upi;
        this.password = password;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.sending_dialog, null);
        setTitle("Sending...");
        addContentView(contentView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));

        // scale length
        statusView = (TextView)contentView.findViewById(R.id.statusText);
        String text = "";
        statusView.setText(text);

        // button bar
        cancelButton = (Button)contentView.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                unsubscribe();
                dismiss();
            }
        });

        okButton = (Button)contentView.findViewById(R.id.okButton);
        okButton.setEnabled(false);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        try {
            serverAddress = new URL(readServerAddress());
            mail(upi, password);
        } catch (MalformedURLException e) {
            setError("Can't connect to server.");
        }
    }

    private void unsubscribe() {
        if (currentSubscription != null) {
            currentSubscription.unsubscribe();
            currentSubscription = null;
        }
    }
    private void setError(String message) {
        unsubscribe();

        statusView.setText(message);
        cancelButton.setEnabled(false);
        okButton.setEnabled(true);
    }

    private void setProgress(String message) {
        statusView.setText(message);
    }

    private String readServerAddress() {
        File baseDir = getContext().getExternalFilesDir(null);
        File file = new File(baseDir, "config");
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            return reader.readLine();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    private void mail(final String upi, final String password) {
        JsonLogin jsonLogin = new JsonLogin();

        setProgress("login...");
        currentSubscription = jsonLogin.login(serverAddress, upi, password).subscribeOn(Schedulers.threadPoolForIO())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable throwable) {
                        setError("login failed");
                    }

                    @Override
                    public void onNext(Boolean results) {
                        System.out.print(results);

                        if (results) {
                            setProgress("login ok");
                            uploadData();
                        } else
                            setError("login failed");
                    }
                });
    }

    private void setDone() {
        okButton.setEnabled(true);
        cancelButton.setEnabled(false);
    }

    private void uploadData() {
        setMessage("Upload");
        JsonUpload jsonUpload = new JsonUpload(attachments, groupMembers);
        jsonUpload.upload(serverAddress).subscribeOn(Schedulers.threadPoolForIO())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<JsonUpload.Progress>() {
                    @Override
                    public void onCompleted() {
                        setDone();
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        setError("Upload failed");
                    }

                    @Override
                    public void onNext(JsonUpload.Progress progress) {
                        setMessage("Upload: " + progress.info);
                    }
                });
    }

    public void setGroupMembers(GroupMembers groupMembers) {
        this.groupMembers = groupMembers;
    }

    public GroupMembers getGroupMembers() {
        return groupMembers;
    }

    public void setAttachments(List<Uri> attachments) {
        this.attachments = attachments;
    }

    public List<Uri> getAttachments() {
        return attachments;
    }
}
