package com.example.chatapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.chatapp.databinding.ActivityChatwindoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class Chatwindo : AppCompatActivity()  {
    private lateinit var binding: ActivityChatwindoBinding
    private lateinit var reciverimg: String
    private lateinit var reciverUid: String
    private lateinit var reciverName: String
    private var imageURI: Uri? = null
    private lateinit var SenderUID: String
    private lateinit var senderRoom: String
    private lateinit var reciverRoom: String
    private lateinit var messagesArrayList: ArrayList<MsgModelClass>
    private lateinit var mMessagesAdapter: MessagesAdapter
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private val CONTACT_PICK_REQUEST = 101
    private val PERMISSIONS_REQUEST_READ_CONTACTS = 1
    private lateinit var progressDialog: ProgressDialog


    companion object {
        var senderImg: String = ""
        var reciverIImg: String = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatwindoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        progressDialog = ProgressDialog(this@Chatwindo)
        progressDialog.setMessage("Please wait Data Sending...")
        progressDialog.setCancelable(false)
        supportActionBar?.hide()
        requestContactsPermission()
        database = FirebaseDatabase.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()

        reciverName = intent.getStringExtra("nameeee") ?: ""
        reciverimg = intent.getStringExtra("reciverImg") ?: ""
        reciverUid = intent.getStringExtra("uid") ?: ""

        senderRoom = intent.getStringExtra("senderRoom") ?: ""

      /*  val selectedContacts = intent.getParcelableArrayListExtra<ContactVO>("selectedContacts")

        Log.d("TAG", "this is selected contact list$selectedContacts: ")*/
        messagesArrayList = ArrayList()

        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.stackFromEnd = true
        binding.msgadpter.layoutManager = linearLayoutManager
        mMessagesAdapter = MessagesAdapter(this, messagesArrayList, senderRoom, senderImg, reciverIImg)
        binding.msgadpter.adapter = mMessagesAdapter
        Glide.with(this@Chatwindo).load(reciverimg).into(binding.profileimgg);


        binding.msgadpter.adapter = mMessagesAdapter


        binding.recivername.setText(reciverName)


        SenderUID = firebaseAuth.uid ?: ""

        senderRoom = SenderUID + reciverUid
        reciverRoom = reciverUid + SenderUID


        loadChatData()

        binding.imageOpen.setOnClickListener {
          showTheAllData()
        }

        binding.sendbtnn.setOnClickListener {
            sendTheData()
            binding.customDialog.root.visibility = View.GONE

        }

        binding.leftBtn.setOnClickListener {
            startActivity(Intent(this@Chatwindo,HomeActivity::class.java))
            finish()
        }


    }

    private fun requestContactsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.READ_CONTACTS),
                PERMISSIONS_REQUEST_READ_CONTACTS
            )
        } else {
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                Log.d("TAG", "Permission denied: ")
            }
        }
    }
    override fun onResume() {
        super.onResume()
        loadChatData()
    }

    private fun loadChatData() {
        val chatReference: DatabaseReference = database.reference.child("chats").child(reciverRoom).child("messages")
        val reference: DatabaseReference = database.reference.child("user").child(firebaseAuth.uid ?: "")

        chatReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                messagesArrayList.clear()
                for (dataSnapshot in snapshot.children) {
                    val messages = dataSnapshot.getValue(MsgModelClass::class.java)
                    messagesArrayList.add(messages!!)
                }
                mMessagesAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                senderImg = snapshot.child("profilepic").getValue(String::class.java) ?: ""
                reciverIImg = reciverimg
            }

            override fun onCancelled(error: DatabaseError) {}
        })


    }


    private fun showTheAllData() {
        if (binding.customDialog.root.visibility == View.VISIBLE) {
            binding.customDialog.root.visibility = View.GONE
        } else {
            binding.customDialog.root.visibility = View.VISIBLE
        }

        binding.customDialog.imageShow.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), 10)
            binding.customDialog.root.visibility = View.GONE

        }
        binding.customDialog.videoShow.setOnClickListener {
            val intent = Intent()
            intent.type = "video/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Video"), 10)
            binding.customDialog.root.visibility = View.GONE
        }
        binding.customDialog.audioShow.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "audio/*" // Set MIME type to audio/*
            startActivityForResult(Intent.createChooser(intent, "Select Audio"), 10)
            binding.customDialog.root.visibility = View.GONE

        }
        binding.customDialog.cameraShow.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE) // Use ACTION_IMAGE_CAPTURE for capturing images
            if (intent.resolveActivity(packageManager) != null) {
                startActivityForResult(intent, 10)
                binding.customDialog.root.visibility = View.GONE

            } else {
                Toast.makeText(this, "Camera app not found", Toast.LENGTH_SHORT).show()
                binding.customDialog.root.visibility = View.GONE
            }
        }
        binding.customDialog.contactShow.setOnClickListener {
            pickContact()

        }


        binding.customDialog.documentShow.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*"
            startActivityForResult(Intent.createChooser(intent, "Select Document"), 10)
            binding.customDialog.root.visibility = View.GONE

        }
    }

    private fun pickContact() {
        val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
        startActivityForResult(intent, CONTACT_PICK_REQUEST)
        binding.customDialog.root.visibility = View.GONE

    }

    @SuppressLint("Range")
    private fun getContactInfo(contactUri: Uri): String {
        val contentResolver = applicationContext.contentResolver
        var name: String? = null
        var phoneNumber: String? = null

        // Query the contact data based on the contact URI
        val cursor = contentResolver.query(contactUri, null, null, null, null)
        cursor?.use { cursor ->
            if (cursor.moveToFirst()) {
                // Extract the contact name
                val nameColumnIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                name = cursor.getString(nameColumnIndex)

                // Check if the contact has a phone number
                val hasPhoneNumber =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)).toInt()
                if (hasPhoneNumber > 0) {
                    // Query the phone numbers associated with the contact
                    val phoneNumberCursor = contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        arrayOf(contactUri.lastPathSegment),
                        null
                    )
                    phoneNumberCursor?.use { phoneCursor ->
                        if (phoneCursor.moveToFirst()) {
                            // Extract the phone number
                            val phoneNumberColumnIndex =
                                phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                            phoneNumber = phoneCursor.getString(phoneNumberColumnIndex)
                        }
                    }
                }
            }
        }
        return "$name \n$phoneNumber"
    }

    private fun sendTheData() {
        val message = binding.textmsg.text.toString()

        if (message.isEmpty()) {
            return
        }
        binding.textmsg.setText("")
        val date = Date()
        val messages = MsgModelClass(message, SenderUID, date.time)
        Log.d("TAG", "This is timer:$date ")

        // Save the message to the sender's room
        database.reference.child("chats")
            .child(senderRoom)
            .child("messages")
            .push().setValue(messages)
            .addOnCompleteListener { senderTask ->
                if (senderTask.isSuccessful) {
                    // Save the message to the receiver's room
                    database.reference.child("chats")
                        .child(reciverRoom)
                        .child("messages")
                        .push().setValue(messages)
                        .addOnCompleteListener { receiverTask ->
                            if (receiverTask.isSuccessful) {
                                // Message sent successfully to both sender and receiver
                                Log.d("sendtheData", "Message sent successfully")
                            } else {
                                // Handle failure to send message to receiver
                                Log.e("sendtheData", "Failed to send message to receiver")
                            }
                        }
                } else {
                    // Handle failure to send message to sender
                    Log.e("sendtheData", "Failed to send message to sender")
                }
            }
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            10 -> {
                if (resultCode == RESULT_OK) {
                    data?.data?.let { mediaUri ->
                        when {
                            isImage(mediaUri) -> {
                                val message = "Sent an image"
                                sendMessage(message, mediaUri) // Send the image
                            }
                            isVideo(mediaUri) -> {
                                val message = "Sent a video"
                                sendMessage(message, videoUri = mediaUri) // Send the video
                            }
                            isAudio(mediaUri) -> {
                                val message = "Sent an audio"
                                sendMessage(message, audioUri = mediaUri) // Send the audio
                            }
                            isDocument(mediaUri) -> {
                                val message = "Sent a document"
                                sendMessage(message, documentUri = mediaUri) // Send the document
                            }
                            isContact(mediaUri) -> {
                                val message = "Sent a contact"
                                val contactUrl = getContactUrl(mediaUri) // Get the contact URL
                                sendMessage(message, contactUrl = contactUrl) // Send the contact
                            }
                            else -> {
                                // Handle other types of media if needed
                            }
                        }
                    }
                }
            }
            CONTACT_PICK_REQUEST -> {
                if (resultCode == Activity.RESULT_OK) {
                    data?.data?.let { contactUri ->
                        // Retrieve contact information and send it
                        val contactInfo = getContactInfo(contactUri)
                        sendMessage("$contactInfo")
                    }
                }
            }
        }
    }

    private fun getContactUrl(contactUri: Uri): String? {
        val contentResolver = applicationContext.contentResolver
        val cursor = contentResolver.query(contactUri, null, null, null, null)
        cursor?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameColumnIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                val phoneNumberColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val emailColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)

                val name = cursor.getString(nameColumnIndex)
                val phoneNumber = cursor.getString(phoneNumberColumnIndex)
                val email = cursor.getString(emailColumnIndex)

                Log.d("TAG", "This is name $name and number $phoneNumber ")
                // Format the contact details into a string
                return "$name\n$phoneNumber"
            }
        }
        return ""
    }

    private  fun isContact(uri: Uri?): Boolean {
        // Check if the URI is not null and if it has the correct scheme and authority
        return if (uri != null && uri.scheme == ContentResolver.SCHEME_CONTENT && uri.authority == ContactsContract.AUTHORITY) {
            // Extract the contact ID from the URI
            val contactId = ContentUris.parseId(uri)
            // Check if the contact ID is valid
            contactId != -1L
        } else {
            false
        }
    }

    private fun isDocument(uri: Uri): Boolean {
        return ContentResolver.SCHEME_CONTENT == uri.scheme &&
                (contentResolver.getType(uri)?.startsWith("application/pdf") == true ||
                        contentResolver.getType(uri)?.startsWith("application/msword") == true ||
                        contentResolver.getType(uri)?.startsWith("application/vnd.openxmlformats-officedocument.wordprocessingml.document") == true ||
                        contentResolver.getType(uri)?.startsWith("application/vnd.ms-excel") == true ||
                        contentResolver.getType(uri)?.startsWith("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") == true ||
                        contentResolver.getType(uri)?.startsWith("application/vnd.ms-powerpoint") == true ||
                        contentResolver.getType(uri)?.startsWith("application/vnd.openxmlformats-officedocument.presentationml.presentation") == true ||
                        contentResolver.getType(uri)?.startsWith("application/rtf") == true)
    }

    private fun isAudio(uri: Uri): Boolean {
        return ContentResolver.SCHEME_CONTENT == uri.scheme &&
                "audio" == contentResolver.getType(uri)?.substringBefore("/")
    }
    // Function to check if the selected media is an image
    private fun isImage(uri: Uri): Boolean {
        return ContentResolver.SCHEME_CONTENT == uri.scheme &&
                "image" == this?.contentResolver?.getType(uri)?.substringBefore("/")
    }

    // Function to check if the selected media is a video
    private fun isVideo(uri: Uri): Boolean {
        return ContentResolver.SCHEME_CONTENT == uri.scheme &&
                "video" == this?.contentResolver?.getType(uri)?.substringBefore("/")
    }

    private fun sendMessage(
        message: String,
        imageUri: Uri? = null,
        videoUri: Uri? = null,
        audioUri: Uri? = null,
        documentUri: Uri? = null,
        contactUrl: String? = null // Updated parameter type to String for contact URL
    ) {
        progressDialog.show()


        val date = Date()

        // Check if there's an image, video, audio, document, or contact URL to upload
        if (imageUri != null || videoUri != null || audioUri != null || documentUri != null || contactUrl != null) {
            // Generate a unique name for the media file
            val mediaName = UUID.randomUUID().toString()

            // Reference to the Firebase Storage location to store the media file
            val storageReference = when {
                imageUri != null -> FirebaseStorage.getInstance().reference.child("images/$mediaName")
                videoUri != null -> FirebaseStorage.getInstance().reference.child("videos/$mediaName")
                audioUri != null -> FirebaseStorage.getInstance().reference.child("audios/$mediaName")
                documentUri != null -> FirebaseStorage.getInstance().reference.child("documents/$mediaName")
                else -> null
            }

            storageReference?.let { reference ->
                // Upload the media file to Firebase Storage
                val uploadTask = when {
                    imageUri != null -> reference.putFile(imageUri)
                    videoUri != null -> reference.putFile(videoUri)
                    audioUri != null -> reference.putFile(audioUri)
                    documentUri != null -> reference.putFile(documentUri)
                    else -> null
                }

                uploadTask?.addOnSuccessListener { taskSnapshot ->
                    // Once the upload is successful, get the download URL of the media file
                    reference.downloadUrl.addOnSuccessListener { mediaUrl ->
                        // Determine the message type based on the presence of image, video, audio, document, or contact URL
                        val messageType = when {
                            imageUri != null -> "image"
                            videoUri != null -> "video"
                            audioUri != null -> "audio"
                            documentUri != null -> "document"
                            contactUrl != null -> "contact"
                            else -> null
                        }

                        messageType?.let { type ->
                            // Create a message object with the appropriate type, sender UID, timestamp, and media URL
                            val messages = when (type) {
                                "contact" -> MsgModelClass(type, SenderUID, date.time, message, mediaUrl.toString())
                                else -> MsgModelClass(type, SenderUID, date.time, mediaUrl.toString(), message)
                            }

                            // Save the message to the Firebase Realtime Database under both sender's and receiver's chat rooms
                            database.reference.child("chats")
                                .child(senderRoom)
                                .child("messages")
                                .push().setValue(messages)
                                .addOnCompleteListener { task ->
                                    // Save the message to the receiver's chat room as well
                                    database.reference.child("chats")
                                        .child(reciverRoom)
                                        .child("messages")
                                        .push().setValue(messages)
                                        .addOnCompleteListener {
                                            progressDialog.dismiss()

                                        }
                                }
                        }
                    }
                }
                    ?.addOnFailureListener { exception ->
                        // Handle any errors that occur during the upload
                        Log.e("sendMessage", "Error uploading media: ${exception.message}")
                        progressDialog.dismiss()

                    } !!
            }
        } else {
            // If there's neither image, video, audio, document, nor contact URL, save the message without media URL
            val messages = MsgModelClass(message, SenderUID, date.time)
            // Save the message to the Firebase Realtime Database under both sender's and receiver's chat rooms
            database.reference.child("chats")
                .child(senderRoom)
                .child("messages")
                .push().setValue(messages)
                .addOnCompleteListener { task ->
                    // Save the message to the receiver's chat room as well
                    database.reference.child("chats")
                        .child(reciverRoom)
                        .child("messages")
                        .push().setValue(messages)
                        .addOnCompleteListener {
                            progressDialog.dismiss()

                        }
                }
        }
    }

    /*private fun sendMessage(
        message: String,
        imageUri: Uri? = null,
        videoUri: Uri? = null,
        audioUri: Uri? = null,
        documentUri: Uri? = null
    ) {
        val date = Date()

        // Check if there's an image, video, audio, or document to upload
        if (imageUri != null || videoUri != null || audioUri != null || documentUri != null) {
            // Generate a unique name for the media file
            val mediaName = UUID.randomUUID().toString()

            // Reference to the Firebase Storage location to store the media file
            val storageReference = when {
                imageUri != null -> FirebaseStorage.getInstance().reference.child("images/$mediaName")
                videoUri != null -> FirebaseStorage.getInstance().reference.child("videos/$mediaName")
                audioUri != null -> FirebaseStorage.getInstance().reference.child("audios/$mediaName")
                documentUri != null -> FirebaseStorage.getInstance().reference.child("documents/$mediaName")
                else -> null
            }

            storageReference?.let { reference ->
                // Upload the media file to Firebase Storage
                val uploadTask = when {
                    imageUri != null -> reference.putFile(imageUri)
                    videoUri != null -> reference.putFile(videoUri)
                    audioUri != null -> reference.putFile(audioUri)
                    documentUri != null -> reference.putFile(documentUri)
                    else -> null
                }

                uploadTask?.addOnSuccessListener { taskSnapshot ->
                    // Once the upload is successful, get the download URL of the media file
                    reference.downloadUrl.addOnSuccessListener { mediaUrl ->
                        // Determine the message type based on the presence of image, video, audio, or document
                        val messageType = when {
                            imageUri != null -> "image"
                            videoUri != null -> "video"
                            audioUri != null -> "audio"
                            documentUri != null -> "document"
                            else -> null
                        }

                        messageType?.let { type ->
                            // Create a message object with the appropriate type, sender UID, timestamp, and media URL
                            val messages = MsgModelClass(type, SenderUID, date.time, mediaUrl.toString(), message)

                            // Save the message to the Firebase Realtime Database under both sender's and receiver's chat rooms
                            database.reference.child("chats")
                                .child(senderRoom)
                                .child("messages")
                                .push().setValue(messages)
                                .addOnCompleteListener { task ->
                                    // Save the message to the receiver's chat room as well
                                    database.reference.child("chats")
                                        .child(reciverRoom)
                                        .child("messages")
                                        .push().setValue(messages)
                                        .addOnCompleteListener { }
                                }
                        }
                    }
                }
                    ?.addOnFailureListener { exception ->
                        // Handle any errors that occur during the upload
                        Log.e("sendMessage", "Error uploading media: ${exception.message}")
                    } !!
            }
        } else {
            // If there's neither image, video, audio, nor document, save the message without media URL
            val messages = MsgModelClass(message, SenderUID, date.time)
            // Save the message to the Firebase Realtime Database under both sender's and receiver's chat rooms
            database.reference.child("chats")
                .child(senderRoom)
                .child("messages")
                .push().setValue(messages)
                .addOnCompleteListener { task ->
                    // Save the message to the receiver's chat room as well
                    database.reference.child("chats")
                        .child(reciverRoom)
                        .child("messages")
                        .push().setValue(messages)
                        .addOnCompleteListener { }
                }
        }
    }
*/

    /* private fun sendMessage(message: String, imageUri: Uri? = null, videoUri: Uri? = null, audioUri: Uri? = null) {
         val date = Date()

         // Check if there's an image, video, or audio to upload
         if (imageUri != null || videoUri != null || audioUri != null) {
             // Generate a unique name for the media file
             val mediaName = UUID.randomUUID().toString()

             // Reference to the Firebase Storage location to store the media file
             val storageReference = when {
                 imageUri != null -> FirebaseStorage.getInstance().reference.child("images/$mediaName")
                 videoUri != null -> FirebaseStorage.getInstance().reference.child("videos/$mediaName")
                 audioUri != null -> FirebaseStorage.getInstance().reference.child("audios/$mediaName")
                 else -> null
             }

             storageReference?.let { reference ->
                 // Upload the media file to Firebase Storage
                 val uploadTask = when {
                     imageUri != null -> reference.putFile(imageUri)
                     videoUri != null -> reference.putFile(videoUri)
                     audioUri != null -> reference.putFile(audioUri)
                     else -> null
                 }

                 return@let uploadTask?.addOnSuccessListener { taskSnapshot ->
                     // Once the upload is successful, get the download URL of the media file
                     reference.downloadUrl.addOnSuccessListener { mediaUrl ->
                         // Determine the message type based on the presence of image, video, or audio
                         val messageType = when {
                             imageUri != null -> "image"
                             videoUri != null -> "video"
                             audioUri != null -> "audio"
                             else -> null
                         }

                         messageType?.let { type ->
                             // Create a message object with the appropriate type, sender UID, timestamp, and media URL
                             val messages = MsgModelClass(type, SenderUID, date.time, mediaUrl.toString())

                             // Save the message to the Firebase Realtime Database under both sender's and receiver's chat rooms
                             database.reference.child("chats")
                                 .child(senderRoom)
                                 .child("messages")
                                 .push().setValue(messages)
                                 .addOnCompleteListener { task ->
                                     // Save the message to the receiver's chat room as well
                                     database.reference.child("chats")
                                         .child(reciverRoom)
                                         .child("messages")
                                         .push().setValue(messages)
                                         .addOnCompleteListener { }
                                 }
                         }
                     }
                 }
                     ?.addOnFailureListener { exception ->
                         // Handle any errors that occur during the upload
                         Log.e("sendMessage", "Error uploading media: ${exception.message}")
                     } !!
             }
         } else {
             // If there's neither image, video, nor audio, save the message without media URL
             val messages = MsgModelClass(message, SenderUID, date.time)
             // Save the message to the Firebase Realtime Database under both sender's and receiver's chat rooms
             database.reference.child("chats")
                 .child(senderRoom)
                 .child("messages")
                 .push().setValue(messages)
                 .addOnCompleteListener { task ->
                     // Save the message to the receiver's chat room as well
                     database.reference.child("chats")
                         .child(reciverRoom)
                         .child("messages")
                         .push().setValue(messages)
                         .addOnCompleteListener { }
                 }
         }
     }
 */

}
