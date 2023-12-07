package com.newsapp.Adapter

import android.content.Context
import android.content.Intent
import android.text.Spannable
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.newsapp.R
import com.newsapp.WebActivity
import com.newsapp.model.NewsData
import com.squareup.picasso.Picasso


class News_Adapter(newsModalArrayList: ArrayList<NewsData>, context: Context) :
    RecyclerView.Adapter<News_Adapter.ViewHolder>() {
    private var newsData = ArrayList<NewsData>()
    private val context: Context
    var isExpanded = false

    init {
        newsData = newsModalArrayList
        this.context = context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cardview, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val modal = newsData[position]


        // Set the initial text
        updateTextView(holder.summary, modal.summary, 100)

        // Set a ClickableSpan to handle "Show More" click
        val spannableString = SpannableString(context.getString(R.string.show_more))
        val clickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val isExpanded = !holder.isExpanded
                holder.isExpanded = isExpanded
                updateTextView(
                    holder.summary,
                    modal.summary,
                    if (isExpanded) modal.summary.length else 100
                )
            }
        }
        spannableString.setSpan(
            clickableSpan,
            0,
            spannableString.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Append "Show More" at the end of the TextView
        holder.summary.append(" ")
        holder.summary.append(spannableString)
        holder.summary.movementMethod = LinkMovementMethod.getInstance()
        holder.title.text = modal.title
        //        holder.summary.setText(modal.getSummary());
        holder.updated_at.text = "Updated at: " + modal.updated_at
        Picasso.get().load(modal.image_url).fit().centerCrop().into(holder.image)

        // Set item click listener
        holder.itemView.setOnClickListener { // Handle item click, open WebView with the URL
            openWebView(modal.url)
        }
    }

    override fun getItemCount(): Int {
        return newsData.size
    }

    fun filterList(filterlist: ArrayList<NewsData>) {
        // below line is to add our filtered
        // list in our course array list.
        newsData = filterlist
        // below line is to notify our adapter
        // as change in recycler view data.
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var isExpanded = false
        val title: TextView
        val summary: TextView
        val updated_at: TextView
        val image: ImageView

        init {
            title = itemView.findViewById(R.id.title)
            summary = itemView.findViewById(R.id.summary)
            updated_at = itemView.findViewById(R.id.updatedat)
            image = itemView.findViewById(R.id.image)
        }
    }

    private fun updateTextView(textView: TextView, fullText: String, charLimit: Int) {
        if (fullText.length <= charLimit) {
            textView.text = fullText
        } else {
            val truncatedText = fullText.substring(0, charLimit)
            val showMoreText = context.getString(R.string.show_more)
            val displayText = if (isExpanded) fullText else "$truncatedText "
            textView.text = displayText
        }
    }

    private fun openWebView(url: String) {
        val intent = Intent(context, WebActivity::class.java)
        intent.putExtra("url", url)
        context.startActivity(intent)
    }
}
