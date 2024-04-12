package com.example.chatapp


import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class UserAdapter(private val homeActivity: HomeActivity, private val usersArrayList: ArrayList<Users>) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(homeActivity).inflate(R.layout.user_item, parent, false)
        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val users = usersArrayList[position]

        // Check if the current user is the same as the user being displayed
        if (users.userId == FirebaseAuth.getInstance().currentUser?.uid) {
            // Don't bind data if the user is the current user
            holder.itemView.visibility = View.GONE
            holder.itemView.layoutParams = RecyclerView.LayoutParams(0, 0)
            return
        }

        // Bind data to the view for other users
        holder.username.text = users.userName
        holder.userstatus.text = users.status
        Picasso.get().load(users.profilepic).into(holder.userimg)

        holder.itemView.setOnClickListener {
            val intent = Intent(homeActivity, Chatwindo::class.java)
            intent.putExtra("nameeee", users.userName)
            intent.putExtra("reciverImg", users.profilepic)
            intent.putExtra("uid", users.userId)
            homeActivity.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return usersArrayList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userimg: CircleImageView = itemView.findViewById(R.id.userimg)
        val username: TextView = itemView.findViewById(R.id.username)
        val userstatus: TextView = itemView.findViewById(R.id.userStatus)
    }
}
