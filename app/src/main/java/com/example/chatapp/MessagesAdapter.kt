package com.example.chatapp


import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color

import android.media.MediaPlayer
import android.net.Uri
import android.os.CountDownTimer
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.VideoView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatapp.Chatwindo.Companion.reciverIImg
import com.example.chatapp.Chatwindo.Companion.senderImg
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import org.w3c.dom.Text
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MessagesAdapter(private val context: Context, private val messagesAdapterArrayList: ArrayList<MsgModelClass>, private val senderRoom: String, private val senderName: String, private val senderImgUrl: String) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val ITEM_SEND = 1
    private val ITEM_RECEIVE = 2




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM_SEND) {
            val view = LayoutInflater.from(context).inflate(R.layout.sender_layout, parent, false)
            SenderViewHolder(view)
        } else {
            val view = LayoutInflater.from(context).inflate(R.layout.reciver_layout, parent, false)
            ReceiverViewHolder(view)
        }
    }

    // Update onBindViewHolder method in MessagesAdapter
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val messages = messagesAdapterArrayList[position]
        if (messages.isSending) {
            if (holder is SenderViewHolder) {
                holder.senderProgressBar.visibility = View.VISIBLE
            }
        } else {
            // Hide progress bar if message is not sending
            if (holder is SenderViewHolder) {
                holder.senderProgressBar.visibility = View.GONE
            }
        }
        holder.itemView.setOnLongClickListener {
            AlertDialog.Builder(context)
                .setTitle("Delete")
                .setMessage("Are you sure you want to delete this message?")
                .setPositiveButton("Yes") { dialogInterface, _ ->
                    val messageId = messagesAdapterArrayList[position].senderId
                    val reference = FirebaseDatabase.getInstance().reference
                    reference.child("chats").child(senderRoom).child("messages").child(messageId!!).removeValue()
                        .addOnSuccessListener {
                            messagesAdapterArrayList.removeAt(position)
                            notifyItemRemoved(position)
                        }
                        .addOnFailureListener { exception ->
                            Log.e("TAG", "Error deleting message: ${exception.message}")
                        }
                    dialogInterface.dismiss()
                }
                .setNegativeButton("No") { dialogInterface, _ -> dialogInterface.dismiss() }
                .show()
            false
        }
        if (messages.isSending) {
            if (holder is SenderViewHolder) {
                holder.senderProgressBar.visibility = View.VISIBLE
            } else if (holder is ReceiverViewHolder) {
                holder.receiverProgressBar.visibility = View.VISIBLE
            }
        } else {
            if (holder is SenderViewHolder) {
                holder.senderProgressBar.visibility = View.GONE
            } else if (holder is ReceiverViewHolder) {
                holder.receiverProgressBar.visibility = View.GONE
            }
        }

        holder.itemView.setOnClickListener {
            // Check if the message contains an image URL
            if (!messages.imageUrl.isNullOrEmpty()) {
                // Create an Intent to start the FullScreenActivity
                val intent = Intent(context, FullScreenActivity::class.java).apply {
                    // Pass the image URL as extra to the FullScreenActivity
                    putExtra("mediaUrl", messages.imageUrl)
                }
                // Start the FullScreenActivity
                context.startActivity(intent)
            }
            else if (!messages.videoUrl.isNullOrEmpty()) {
                // Create an Intent to start the FullScreenActivity
                val intent = Intent(context, VideoOpenActivity::class.java).apply {
                    // Pass the video URL as an extra to the FullScreenActivity
                    putExtra("videoUrl", messages.imageUrl)
                }
                // Start the FullScreenActivity
                context.startActivity(intent)
            }
        }


        if (messages.isSending) {
            if (holder is SenderViewHolder) {
                holder.senderProgressBar.visibility = View.VISIBLE
            } else if (holder is ReceiverViewHolder) {
                holder.receiverProgressBar.visibility = View.VISIBLE
            }
        } else {
            // Hide progress bar if message is not sending
            if (holder is SenderViewHolder) {
                holder.senderProgressBar.visibility = View.GONE
            } else if (holder is ReceiverViewHolder) {
                holder.receiverProgressBar.visibility = View.GONE
            }
        }

        // Display text message
        if (holder is SenderViewHolder) {
            holder.msgText.text = "${messages.message}  ${getTimeFromTimeStamp(messages.timestamp!!)}"
            Glide.with(context).load(senderImg).into(holder.senderMessageImage)
            Glide.with(context).load(senderImg).into(holder.circleImageView)
            holder.msgText.visibility = View.VISIBLE
            holder.senderMessageImage.visibility = View.GONE
            holder.senderMessageVideoView.visibility = View.GONE
            holder.senderMessageAudioButton.visibility = View.GONE
            holder.msgSenderFile.visibility = View.GONE
            holder.textTime.visibility = View.GONE
            holder.constraintLayout1.visibility = View.GONE
        } else if (holder is ReceiverViewHolder) {
            holder.msgText.text = "${messages.message}  ${getTimeFromTimeStamp(messages.timestamp!!)}"
            Glide.with(context).load(reciverIImg).into(holder.receiverMessageImage)
            Glide.with(context).load(reciverIImg).into(holder.circleImageViews)
            holder.msgText.visibility = View.VISIBLE
            holder.receiverMessageImage.visibility = View.GONE
            holder.receiverMessageVideoView.visibility = View.GONE
            holder.receiverPlayAudioButtons.visibility = View.GONE
            holder.reciverTextFile.visibility = View.GONE
            holder.textTimer1.visibility = View.GONE
        }



        // Display text message



       // Display image message
        if (!messages.imageUrl.isNullOrEmpty()) {
            if (holder is SenderViewHolder) {
                Glide.with(context).load(messages.imageUrl).into(holder.senderMessageImage)
                holder.msgText.visibility = View.VISIBLE
                holder.senderMessageImage.visibility = View.VISIBLE
                holder.senderMessageVideoView.visibility = View.GONE
                holder.senderMessageAudioButton.visibility = View.GONE
                holder.msgSenderFile.visibility = View.GONE
                holder.textTime.visibility = View.VISIBLE
                holder.textTime.text = "  ${getTimeFromTimeStamp(messages.timestamp!!)}"
                holder.textTime.setTextColor(Color.DKGRAY)
                holder.constraintLayout1.visibility = View.VISIBLE



            } else if (holder is ReceiverViewHolder) {
                Glide.with(context).load(messages.imageUrl).into(holder.receiverMessageImage)
                holder.msgText.visibility = View.VISIBLE
                holder.receiverMessageImage.visibility = View.VISIBLE
                holder.receiverMessageVideoView.visibility = View.GONE
                holder.receiverPlayAudioButtons.visibility = View.GONE
                holder.reciverTextFile.visibility = View.GONE
                holder.textTimer1.text = "  ${getTimeFromTimeStamp(messages.timestamp!!)}"
                holder.textTimer1.visibility = View.VISIBLE

            }
        }

             // Display video message
               if (!messages.videoUrl.isNullOrEmpty()) {
                   if (holder is SenderViewHolder) {
                       // Load video into VideoView
                       holder.senderMessageVideoView.setVideoURI(Uri.parse(messages.videoUrl))
                       holder.senderMessageVideoView.start() // Start playing the video
                       holder.msgText.visibility = View.VISIBLE
                       holder.senderMessageImage.visibility = View.VISIBLE
                       holder.senderMessageAudioButton.visibility = View.GONE
                       holder.msgSenderFile.visibility = View.GONE
                       holder.textTime.text = "  ${getTimeFromTimeStamp(messages.timestamp!!)}"
                       holder.textTime.setTextColor(Color.DKGRAY)
                       holder.constraintLayout1.visibility = View.VISIBLE

                   } else if (holder is ReceiverViewHolder) {
                       holder.receiverMessageVideoView.setVideoURI(Uri.parse(messages.videoUrl))
                       holder.receiverMessageVideoView.start() // Start playing the video
                       holder.msgText.visibility = View.VISIBLE
                       holder.receiverMessageImage.visibility = View.VISIBLE
                       holder.receiverPlayAudioButtons.visibility = View.GONE
                       holder.reciverTextFile.visibility = View.GONE
                       holder.textTimer1.text = "  ${getTimeFromTimeStamp(messages.timestamp!!)}"
                       holder.textTimer1.visibility = View.VISIBLE

                   }
               }




        // Display audio message
        if (!messages.audioUrl.isNullOrEmpty()) {
            if (holder is SenderViewHolder) {
                holder.senderMessageAudioButton.setOnClickListener {
                    val mediaPlayer = MediaPlayer().apply {
                        setDataSource(messages.audioUrl) // Set audio URL
                        prepareAsync()
                    }
                    mediaPlayer.setOnPreparedListener {
                        it.start() // Start playing the audio
                    }
                }
                holder.msgText.visibility = View.GONE
                holder.senderMessageImage.visibility = View.GONE
                holder.senderMessageVideoView.visibility = View.GONE
                holder.senderMessageAudioButton.visibility = View.VISIBLE
                holder.msgSenderFile.visibility = View.GONE
                holder.textTime.text = "  ${getTimeFromTimeStamp(messages.timestamp!!)}"
                holder.textTime.setTextColor(Color.DKGRAY)
                holder.constraintLayout1.visibility = View.VISIBLE


            } else if (holder is ReceiverViewHolder) {
                holder.receiverPlayAudioButtons.setOnClickListener {
                    val mediaPlayer = MediaPlayer().apply {
                        setDataSource(messages.audioUrl) // Set audio URL
                        prepareAsync()
                    }
                    mediaPlayer.setOnPreparedListener {
                        it.start() // Start playing the audio
                    }
                }
                holder.msgText.visibility = View.GONE
                holder.receiverMessageImage.visibility = View.GONE
                holder.receiverMessageVideoView.visibility = View.GONE
                holder.receiverPlayAudioButtons.visibility = View.VISIBLE
                holder.reciverTextFile.visibility = View.GONE
                holder.textTimer1.text = "  ${getTimeFromTimeStamp(messages.timestamp!!)}"
                holder.textTimer1.visibility = View.VISIBLE

            }
        }

        // Display text message
                     if (holder is SenderViewHolder) {
                         holder.msgSenderFile.text = messages.message
                         // Load sender image
                        // Glide.with(context).load(senderImg).into(holder.senderMessageImage)
                         // Load sender circular image
                         Glide.with(context).load(senderImg).into(holder.circleImageView)
                         // Check if the message contains a document URI or path
                         if (messages.documentUri != null) {
                             holder.itemView.setOnClickListener {
                                 // Open the document using an appropriate viewer
                                 openDocument(messages.documentUri)
                             }
                         }
                         holder.textTime.text = "  ${getTimeFromTimeStamp(messages.timestamp!!)}"
                         holder.textTime.setTextColor(Color.DKGRAY)
                         holder.constraintLayout1.visibility = View.VISIBLE



                     } else if (holder is ReceiverViewHolder) {
                         holder.reciverTextFile.text = messages.message
                         // Load receiver image
                         //Glide.with(context).load(reciverIImg).into(holder.receiverMessageImage)
                         // Load receiver circular image
                         Glide.with(context).load(reciverIImg).into(holder.circleImageViews)
                         // Check if the message contains a document URI or path
                         if (messages.documentUri != null) {
                             holder.itemView.setOnClickListener {
                                 // Open the document using an appropriate viewer
                                 openDocument(messages.documentUri)
                             }
                         }
                         holder.textTimer1.text = "  ${getTimeFromTimeStamp(messages.timestamp!!)}"


                     }


    }
    fun getTimeFromTimeStamp(timestamp: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        val dateFormat = SimpleDateFormat("\nhh:mm:ss a", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }



    private fun openDocument(documentUri: String) {
        // Direct link to the PDF file in Google Drive
        val directLink = "https://drive.google.com/uc?export=download&id=$documentUri"

        // Create a URI object from the direct link
        val uri = Uri.parse(directLink)

        // Create an intent to view the PDF
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, "application/pdf")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // Handle no PDF viewer app installed scenario
        }
    }

//  private fun openDocument(documentUri: String) {
//      // Parse the string representation of the URI to create a Uri object
//      val uri = Uri.parse(documentUri)
//
//      val intent = Intent(Intent.ACTION_VIEW)
//      intent.setDataAndType(uri, "application/*") // Adjust MIME type based on the document type
//      intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//
//      try {
//          context.startActivity(intent)
//      } catch (e: ActivityNotFoundException) {
//          // Handle no document viewer installed scenario
//      }
//  }

  override fun getItemCount(): Int {
      return messagesAdapterArrayList.size
  }

  override fun getItemViewType(position: Int): Int {
      val messages = messagesAdapterArrayList[position]
      return if (FirebaseAuth.getInstance().currentUser?.uid == messages.senderId) {
          ITEM_SEND
      } else {
          ITEM_RECEIVE
      }
  }

  inner class SenderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
      val circleImageView: CircleImageView = itemView.findViewById(R.id.profilerggg)
      val msgText: TextView = itemView.findViewById(R.id.msgsendertyp)
      val msgSenderFile: TextView = itemView.findViewById(R.id.msgSenderFile)
      val senderMessageImage: ImageView = itemView.findViewById(R.id.sender_message_image)
      val  senderMessageVideoView:VideoView = itemView.findViewById(R.id.senderMessageVideoView)
      val  senderMessageAudioButton:Button = itemView.findViewById(R.id.senderMessageAudioButton)
      val senderProgressBar:ProgressBar = itemView.findViewById(R.id.senderProgressBar)
      val textTime : TextView = itemView.findViewById(R.id.timerTextSender)
      val constraintLayout1 : ConstraintLayout = itemView.findViewById(R.id.constraintLayout1)
  }

  inner class ReceiverViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
      val circleImageViews: CircleImageView = itemView.findViewById(R.id.pro)
      val msgText: TextView = itemView.findViewById(R.id.recivertextset)
      val reciverTextFile: TextView = itemView.findViewById(R.id.reciverTextFile)
      val receiverMessageImage: ImageView = itemView.findViewById(R.id.receiver_message_image)
      val receiverMessageVideoView:VideoView = itemView.findViewById(R.id.receiverMessageVideoView)
      val receiverPlayAudioButtons:Button = itemView.findViewById(R.id.receiverPlayAudioButton)
      val receiverProgressBar:ProgressBar = itemView.findViewById(R.id.receiverProgressBar)
      val textTimer1 : TextView = itemView.findViewById(R.id.timerTextReciver)



  }
}
