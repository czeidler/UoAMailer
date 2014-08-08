/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.mailer;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.File;
import java.net.*;
import java.util.ArrayList;
import java.util.List;


public class Mailer extends Activity {
    private GroupMembers groupMembers = new GroupMembers();

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        final Button sendButton = (Button)findViewById(R.id.sendButton);

        final EditText upiEditText = (EditText)findViewById(R.id.upiEditText);
        final EditText passwordEditText = (EditText)findViewById(R.id.passwordEditText);
        final GroupMemberView groupMemberView = (GroupMemberView)findViewById(R.id.groupMemberView);
        final Button addButton = (Button)findViewById(R.id.addButton);

        groupMemberView.setGroupMembers(groupMembers);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String upi = upiEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                showSendingDialog(upi, password);
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                groupMembers.addMember("");
            }
        });

        CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
    }

    private void showSendingDialog(String upi, String password) {
        Intent intent = getIntent();
        List<Uri> attachments = null;
        Bundle extras = intent.getExtras();
        if (extras != null) {
            Uri attachment = (Uri)intent.getExtras().get(Intent.EXTRA_STREAM);
            if (attachment != null) {
                attachments = new ArrayList<>();
                attachments.add(attachment);
            } else
                attachments = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        }

        SendingDialog sendingDialog = new SendingDialog(this, upi, password);
        sendingDialog.setAttachments(attachments);
        sendingDialog.setGroupMembers(groupMembers);
        sendingDialog.show();
    }

}
