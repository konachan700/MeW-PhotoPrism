package com.mewhpm.mewphotoprism.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mewhpm.mewphotoprism.Const
import com.mewhpm.mewphotoprism.R
import com.mewhpm.mewphotoprism.entity.AccountEntity
import com.mewhpm.mewphotoprism.view_holders.ListItemViewHolder

class AccountsListAdapter(
    private val accounts : List<AccountEntity>,
    private val onAction : (type : Int, position: Int, item : AccountEntity) -> Unit
) : RecyclerView.Adapter<ListItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListItemViewHolder {
        val view: View = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.layout_account_item, parent, false)
        return ListItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListItemViewHolder, position: Int) {
        val item = accounts[position]
        holder.txtHeader.text = item.name
        holder.txtText.text = item.url
        holder.icon.setImageResource(R.drawable.icon_remote_gallery)
        holder.rootView.setOnClickListener {
            onAction.invoke(Const.ACTION_SHORT_CLICK, position, item)
        }
        holder.rootView.setOnLongClickListener {
            onAction.invoke(Const.ACTION_LONG_CLICK, position, item)
            return@setOnLongClickListener false
        }
    }

    override fun getItemCount(): Int {
        return accounts.size
    }
}