package org.cosmicide.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.cosmicide.databinding.ConversationItemReceivedBinding
import org.cosmicide.databinding.ConversationItemSentBinding
import org.cosmicide.util.CommonUtils

class ConversationAdapter :
    RecyclerView.Adapter<BindableViewHolder<ConversationAdapter.Conversation, *>>() {

    private val conversations = mutableListOf<Conversation>()

    data class Conversation(
        var text: String = "",
        val author: String = "assistant"
    )

    companion object {
        const val VIEW_TYPE_SENT = 1
        const val VIEW_TYPE_RECEIVED = 2
    }

    fun add(conversation: Conversation) {
        conversations += conversation
        notifyItemInserted(conversations.lastIndex)
    }

    fun getConversations(): List<Pair<String, String>> {
        return conversations.map { Pair(it.author, it.text) }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BindableViewHolder<Conversation, *> {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_SENT -> SentViewHolder(
                ConversationItemSentBinding.inflate(
                    inflater,
                    parent,
                    false
                )
            )

            VIEW_TYPE_RECEIVED -> ReceivedViewHolder(
                ConversationItemReceivedBinding.inflate(
                    inflater,
                    parent,
                    false
                )
            )

            else -> throw IllegalArgumentException("Invalid view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: BindableViewHolder<Conversation, *>, position: Int) {
        holder.bind(conversations[position])
    }

    override fun getItemCount() = conversations.size

    override fun getItemViewType(position: Int): Int {
        val conversation = conversations[position]
        return if (conversation.author == "user") {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }

    fun clear() {
        conversations.clear()
        notifyItemRangeRemoved(0, itemCount)
    }

    inner class SentViewHolder(itemBinding: ConversationItemSentBinding) :
        BindableViewHolder<Conversation, ConversationItemSentBinding>(itemBinding) {

        override fun bind(data: Conversation) {
            binding.message.apply {
                CommonUtils.getMarkwon().setMarkdown(this, data.text)
            }
        }
    }

    inner class ReceivedViewHolder(itemBinding: ConversationItemReceivedBinding) :
        BindableViewHolder<Conversation, ConversationItemReceivedBinding>(itemBinding) {

        override fun bind(data: Conversation) {
            binding.message.apply {
                CommonUtils.getMarkwon().setMarkdown(this, data.text)
            }
        }
    }
}
