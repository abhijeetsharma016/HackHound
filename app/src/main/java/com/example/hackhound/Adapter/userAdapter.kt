package com.example.hackhound.Adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.hackhound.R
import com.example.hackhound.model.UserModel

class UserAdapter(private val userList: ArrayList<UserModel>) :
    RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameText: TextView = itemView.findViewById(R.id.tvName)
        val phoneText: TextView = itemView.findViewById(R.id.tvPhone)
        val meal1Text: TextView = itemView.findViewById(R.id.tvMeal1)
        val meal2Text: TextView = itemView.findViewById(R.id.tvMeal2)
        val meal3Text: TextView = itemView.findViewById(R.id.tvMeal3)
        val meal4Text: TextView = itemView.findViewById(R.id.tvMeal4)
        val meal5Text: TextView = itemView.findViewById(R.id.tvMeal5)
        val meal6Text: TextView = itemView.findViewById(R.id.tvMeal6)
        val meal7Text: TextView = itemView.findViewById(R.id.tvMeal7)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.user_item, parent, false)
        return UserViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val currentUser = userList[position]

        // Set basic user info
        holder.nameText.text = currentUser.name
        holder.phoneText.text = currentUser.phone

        // Update meal indicators
        updateMealIndicator(holder.meal1Text, currentUser.time1)
        updateMealIndicator(holder.meal2Text, currentUser.time2)
        updateMealIndicator(holder.meal3Text, currentUser.time3)
        updateMealIndicator(holder.meal4Text, currentUser.time4)
        updateMealIndicator(holder.meal5Text, currentUser.time5)
        updateMealIndicator(holder.meal6Text, currentUser.time6)
        updateMealIndicator(holder.meal7Text, currentUser.time7)

        // Add tooltips to show meal times when available
        setMealTooltip(holder.meal1Text, currentUser.time1, 1)
        setMealTooltip(holder.meal2Text, currentUser.time2, 2)
        setMealTooltip(holder.meal3Text, currentUser.time3, 3)
        setMealTooltip(holder.meal4Text, currentUser.time4, 4)
        setMealTooltip(holder.meal5Text, currentUser.time5, 5)
        setMealTooltip(holder.meal6Text, currentUser.time6, 6)
        setMealTooltip(holder.meal7Text, currentUser.time7, 7)
    }

    private fun updateMealIndicator(textView: TextView, mealTime: String?) {
        if (!mealTime.isNullOrEmpty()) {
            // Meal has been served - set to green
            textView.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#4CAF50")) // Green
            textView.setTextColor(Color.WHITE)
        } else {
            // Meal not served - keep default gray
            textView.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#DDDDDD")) // Gray
            textView.setTextColor(Color.BLACK)
        }
    }

    private fun setMealTooltip(textView: TextView, mealTime: String?, mealNumber: Int) {
        if (!mealTime.isNullOrEmpty()) {
            ViewCompat.setTooltipText(textView, "Meal $mealNumber served at $mealTime")
        } else {
            ViewCompat.setTooltipText(textView, "Meal $mealNumber not served yet")
        }
    }

    override fun getItemCount() = userList.size
}