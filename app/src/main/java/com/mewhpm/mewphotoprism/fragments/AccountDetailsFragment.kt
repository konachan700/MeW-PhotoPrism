package com.mewhpm.mewphotoprism.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.mewhpm.mewphotoprism.AppDatabase
import com.mewhpm.mewphotoprism.Const
import com.mewhpm.mewphotoprism.R
import com.mewhpm.mewphotoprism.entity.AccountEntity

class AccountDetailsFragment : Fragment() {
    private var name:       String? = null
    private var url:        String? = null
    private var username:   String? = null
    private var password:   String? = null
    private var uid:        Long?   = null

    private lateinit var db : AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            name        = it.getString(Const.ARG_ACCOUNT_NAME)
            url         = it.getString(Const.ARG_ACCOUNT_URL)
            username    = it.getString(Const.ARG_ACCOUNT_USERNAME)
            password    = it.getString(Const.ARG_ACCOUNT_PASSWORD)
            uid         = it.getLong(Const.ARG_ACCOUNT_UID)
        }
        db = AppDatabase.getDB(requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_account_details, container, false)
        view.findViewById<Button>(R.id.btnSave).setOnClickListener {
            name        = view.findViewById<EditText>(R.id.txtAccName).text.trim().toString()
            url         = view.findViewById<EditText>(R.id.txtURL).text.trim().toString()
            username    = view.findViewById<EditText>(R.id.txtLogin).text.trim().toString()
            password    = view.findViewById<EditText>(R.id.txtPassword).text.toString()

            if (name == null || name!!.isEmpty()) {
                Toast.makeText(requireContext(), "Account name cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (url == null || url!!.isEmpty()) {
                Toast.makeText(requireContext(), "URL name cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (uid != null && uid != 0L) {
                val item  = db.AccountsDAO().getByUID(uid!!)
                item.name = name
                item.url  = url
                item.user = username
                item.pass = password
                db.AccountsDAO().update(item)
            } else {
                val item = AccountEntity(0, name, url, username, password, Const.XTYPE_PHOTOPRISM)
                db.AccountsDAO().insertAll(item)
            }
            requireActivity().supportFragmentManager.popBackStack()
        }
        view.findViewById<Button>(R.id.btnBack).setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
        val btnDelete = view.findViewById<Button>(R.id.btnDelete)
        btnDelete.visibility = if (uid != null) View.VISIBLE else View.INVISIBLE
        btnDelete.setOnClickListener {
            if (uid != null) {
                val item  = db.AccountsDAO().getByUID(uid!!)
                db.AccountsDAO().delete(item)
                requireActivity().supportFragmentManager.popBackStack()
            }
        }
        if (uid != null) {
            view.findViewById<EditText>(R.id.txtAccName).setText(name ?: "")
            view.findViewById<EditText>(R.id.txtURL).setText(url ?: "")
            view.findViewById<EditText>(R.id.txtLogin).setText(username ?: "")
            view.findViewById<EditText>(R.id.txtPassword).setText(password ?: "")
        }
        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(account : AccountEntity?) =
            AccountDetailsFragment().apply {
                arguments = Bundle().apply {
                    putString(Const.ARG_ACCOUNT_NAME,     account?.name ?: "")
                    putString(Const.ARG_ACCOUNT_URL,      account?.url  ?: "")
                    putString(Const.ARG_ACCOUNT_USERNAME, account?.user ?: "")
                    putString(Const.ARG_ACCOUNT_PASSWORD, account?.pass ?: "")
                    if (account?.uid != null) putLong(Const.ARG_ACCOUNT_UID, account.uid)
                }
            }
    }
}