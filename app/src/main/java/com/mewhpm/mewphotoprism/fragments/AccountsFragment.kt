package com.mewhpm.mewphotoprism.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mewhpm.mewphotoprism.*
import com.mewhpm.mewphotoprism.adapters.AccountsListAdapter

class AccountsFragment : Fragment() {
    private lateinit var db : AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        db = AppDatabase.getDB(requireContext())
        val list = db.AccountsDAO().findAll()
        val recyclerView = requireView().findViewById<RecyclerView>(R.id.accountsList)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 1)
        recyclerView.adapter = AccountsListAdapter(list) { type, position, item ->
            when (type) {
                Const.ACTION_SHORT_CLICK -> {
                    val settings = requireActivity().getSharedPreferences(Const.SHARED_SETTINGS_NAME, 0)
                    val editor = settings.edit()
                    editor.putLong(Const.SHARED_SETTINGS_VAL_UID, item.uid)
                    editor.apply()

                    val transaction = requireActivity().supportFragmentManager.beginTransaction()
                    transaction.replace(R.id.fragmentHost, ImageListFragment.newInstance(item), "MainFragment")
                    transaction.commit()
                }
                Const.ACTION_LONG_CLICK -> {
                    val transaction = requireActivity().supportFragmentManager.beginTransaction()
                    transaction.replace(R.id.fragmentHost, AccountDetailsFragment.newInstance(item), "MainFragment")
                    transaction.addToBackStack(null)
                    transaction.commit()
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_accounts, container, false)
        view.findViewById<FloatingActionButton>(R.id.btnAddAccount).setOnClickListener {
            val fragment = AccountDetailsFragment.newInstance(null)
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragmentHost, fragment, "MainFragment")
            transaction.addToBackStack(null)
            transaction.commit()
        }
        return view
    }

    companion object {
        @JvmStatic
        fun newInstance() = AccountsFragment()
    }
}