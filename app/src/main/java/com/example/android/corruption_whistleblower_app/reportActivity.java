package com.example.android.corruption_whistleblower_app;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.corruption_whistleblower_app.Model.Post;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static com.firebase.ui.auth.AuthUI.TAG;

public class reportActivity extends BaseActivity {

    private final int PICK_IMAGE_REQUEST = 71;
    private Uri filePath;
    private TextView imageView;
    private LocationManager LocationManager;
   private  double lon=36.825358;
   private double lat=-1.291244;
   private  String key;
   private  String date;

    private static final String REQUIRED = "Required";

    // [START declare_database_ref]
    private DatabaseReference mDatabase;
    // [END declare_database_ref]

    private EditText reportField;
    private EditText badgeNoField;
    private Button location2;
    private EditText fullName;
    private EditText emailAddress;
    private Button attachImage;
    private Button submitButton;
    private EditText locationIncident;
    // FirebaseDatabase m;
    StorageReference storageReference;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    //DatabaseReference myRef = database.getReference("reports");
    ScrollView coordinatorLayout;
    LinearLayout coordinatorLayout1;
    private String imageString = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);


        // [START initialize_database_ref]
        mDatabase = FirebaseDatabase.getInstance().getReference("Reports");
        // [END initialize_database_ref]

        reportField = findViewById(R.id.incidenceReport);
        badgeNoField = findViewById(R.id.badgeNo);
        imageView = findViewById(R.id.imageView);
        location2 = findViewById(R.id.locationButton);
        fullName = findViewById(R.id.fullName);
        emailAddress = findViewById(R.id.emailAddress);
        submitButton = findViewById(R.id.submitButton);
        attachImage = findViewById(R.id.buttonAttachImage);
        locationIncident = findViewById(R.id.locationIncident);

        coordinatorLayout1 = (LinearLayout) findViewById(R.id.coordinatorLayout1);


        location2.setOnClickListener(new View.OnClickListener() {


            //choosing the image
            @Override
            public void onClick(View arg0) {

                // Acquire a reference to the system Location Manager
                LocationManager locationManager =
                        (LocationManager) reportActivity.this.getSystemService(Context.LOCATION_SERVICE);
                // Define a listener that responds to location updates
                LocationListener locationListener = new LocationListener() {
                    public void onLocationChanged(Location location) {
                        // Called when a new location is found by the network location provider.
                         lat = (location.getLatitude());
                         lon = (location.getLongitude());

                    }

                    public void onStatusChanged(String provider, int status, Bundle extras) {
                    }

                    public void onProviderEnabled(String provider) {
                    }

                    public void onProviderDisabled(String provider) {
                    }

                };

                // Register the listener with the Location Manager to receive location updates
                if (ActivityCompat.checkSelfPermission(reportActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(reportActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                locationIncident.setText("https://maps.google.com/maps?q=" + lat + "," + lon);
            }

        });

        attachImage.setOnClickListener(new View.OnClickListener(){


            //choosing the image
            @Override
            public void onClick (View v){

                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);

            }
        });
        submitButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                submitPost();
                return;

//                Snackbar.make(coordinatorLayout1, "SUBMITTED SUCCESFULLY...CASE REFERENCE NO" +key, Snackbar.LENGTH_INDEFINITE).show();
//                Snackbar snackbar = Snackbar
//                        .make(coordinatorLayout1, "SUBMITED SUCCESFULLY ...CASE REFERENCE NO;" +key, Snackbar.LENGTH_LONG)
//                        .setAction("COPY", new View.OnClickListener() {
//                            @Override
//                            public void onClick(View view) {
//                                Snackbar snackbar1 = Snackbar.make(coordinatorLayout1, key, Snackbar.LENGTH_INDEFINITE);
//                                snackbar1.show();
//                            }
//                        });

                //snackbar.show();
            }
        });
    }

    public void submitPost() {
        final String report = reportField.getText().toString();
        final String badgeNo = badgeNoField.getText().toString();
        final String image = imageString;
        final String location = locationIncident.getText().toString();
        final String name = fullName.getText().toString();
        final String email = emailAddress.getText().toString();
        final String caseSerialNo="key";
        final String date=Calendar.getInstance().getTime().toString();
        // Title is required
        if (TextUtils.isEmpty(report)) {
            reportField.setError(REQUIRED);
            return;
        }

        // Body is required
        if (TextUtils.isEmpty(badgeNo)) {
            badgeNoField.setError(REQUIRED);
            return;
        }
        // Body is required
        if (TextUtils.isEmpty(location)) {
            locationIncident.setError(REQUIRED);
            return;
        }


        // Disable button so there are no multi-posts
        // setEditingEnabled(false);
        // Toast.makeText(this, "Posting...", Toast.LENGTH_SHORT).show();

        // [START single_value_read]
//      final String userId = getUid();

        mDatabase.child("Reports").addListenerForSingleValueEvent(
                new ValueEventListener() {
        /*final StorageReference fileReference = storageReference.child(System.currentTimeMillis()
                + "." + getFileExtension(filePath));
        uploadTask = fileReference.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {*/
                 //   @Override
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    //public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get user value
                     /*   User user = dataSnapshot.getValue(User.class);

                        // [START_EXCLUDE]
                        if (user == null) {
                            // User is null, error out
                            Log.e(TAG, "User " + userId + " is unexpectedly null");
                            //Toast.makeText(this, "Posting...", Toast.LENGTH_SHORT).show();
                        } else {
                            // Write new post
                            writeNewPost(userId, report, badgeNo, image, location, name, email);



                        }*/
        // Write new post
        writeNewPost(report, badgeNo, image, location, name, email,caseSerialNo,date);
                        // Finish this Activity, back to the stream
                        setEditingEnabled(true);
                        //finish();
                        // [END_EXCLUDE]
                    }
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                        // [START_EXCLUDE]
                        setEditingEnabled(true);
                        // [END_EXCLUDE]
                    }
                });
        // [END single_value_read]
    }

   private void setEditingEnabled(boolean enabled) {
        reportField.setEnabled(enabled);
        badgeNoField.setEnabled(enabled);
        imageView.setEnabled(enabled);
        locationIncident.setEnabled(enabled);
        fullName.setEnabled(enabled);
        emailAddress.setEnabled(enabled);
        if (enabled) {
            //Toast.makeText(this,"loading", Toast.LENGTH_SHORT).show();
          //  mProgressDialog.show();
        } else {
            mProgressDialog.hide();
        }

    }


    // [START write_fan_out]
   private void writeNewPost( String report, String badgeNo, String image, String location, String name, String email, String caseSerialNo,String date) {
        // Create new post at /user-posts/$userid/$postid and at
        // /posts/$postid simultaneously
         key = mDatabase.push().getKey();
        caseSerialNo=key;

        Post post = new Post(report, badgeNo, image, location, name, email,caseSerialNo,date);
        Map<String, Object> postValues = post.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("" + key, postValues);
        //childUpdates.put("/user-posts/" + userId + "/" + key, postValues);
        mDatabase.child(key).setValue(post);
       // mDatabase.updateChildren(childUpdates);
       //Toast.makeText(this,"Upload Successful ...your case refeference is;" +key ,  Toast.LENGTH_LONG).show();
       //final View contextView = findViewById(R.id.emailAddress);
      // coordinatorLayout1=findViewById(R.id.coordinatorLayout1);

//       Snackbar snackbar = Snackbar
//               .make(coordinatorLayout1, "SUBMITED SUCCESFULLY ...CASE REFERENCE NO;" +key, Snackbar.LENGTH_LONG)
//               .setAction("COPY", new View.OnClickListener() {
//                   @Override
//                   public void onClick(View view) {
//                       Snackbar snackbar1 = Snackbar.make(coordinatorLayout1, key, Snackbar.LENGTH_INDEFINITE);
//                       snackbar1.show();
//                   }
//               });
//
//       snackbar.show();
//       Snackbar snackBar = Snackbar.make(findViewById(android.R.id.content), "You need to update your profile", Snackbar.LENGTH_LONG);
//       snackBar.show();

//       AlertDialog.Builder adb=new AlertDialog.Builder(reportActivity.this);
//       adb.setTitle("Title");
//       adb.setMessage("hey there");
//       adb.setPositiveButton("Ok", null);
//       adb.show();
       TextView showText = new TextView(this);
       showText.setText("SUBMITED SUCCESFULLY ...CASE REFERENCE NO;" +key);
// Add the Listener
       showText.setOnLongClickListener(new View.OnLongClickListener() {

           @Override
           public boolean onLongClick(View v) {
               // Copy the Text to the clipboard
               ClipboardManager manager =
                       (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
               TextView showTextParam = (TextView) v;
               manager.setText( showTextParam.getText() );
               // Show a message:
               Toast.makeText(v.getContext(), "Text in clipboard",
                       Toast.LENGTH_SHORT)
                       .show();
               return true;
           }
       });
       AlertDialog.Builder dialog=new AlertDialog.Builder(this,R.style.AlertDialogStyle);
       dialog.setView(showText);
       dialog.setTitle("Dialog Box");
       dialog.setPositiveButton("Ok",
               new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog,
                                       int which) {
                       finish();
                       //Toast.makeText(getApplicationContext(),"Yes is clicked",Toast.LENGTH_LONG).show();
                   }
               });
       AlertDialog alertDialog=dialog.create();
       alertDialog.show();

    }
    // [END write_fan_out]




//set selected image filepath to string visible by user
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        imageView = findViewById(R.id.imageView);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imageView.setText(filePath.toString());
                //change the bitmap to string
                imageString  = BitMapToString(bitmap);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    //convert bitmap to string
    public String BitMapToString(Bitmap bitmap){
        ByteArrayOutputStream baos=new  ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100, baos);
        byte [] b=baos.toByteArray();
        String temp= Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }





}
