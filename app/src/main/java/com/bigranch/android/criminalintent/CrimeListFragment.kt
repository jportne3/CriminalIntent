package com.bigranch.android.criminalintent

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_crime_list.*
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "CrimeListFragment"
private const val DATE = "E MMM dd, yyyy, HH:mm z"

class CrimeListFragment : Fragment() {

    private lateinit var noCrimes: TextView

    /*
    * Required interface for hosting activities
    * */
    interface Callbacks {
        fun onCrimeSelected(crimeId: UUID)
    }

    private var callBacks: Callbacks? = null

    private lateinit var crimeRecyclerView: RecyclerView
    private var adapter: CrimeAdapter? = CrimeAdapter(emptyList())

    private val crimeListViewModel: CrimeListViewModel by lazy {
        ViewModelProviders.of(this).get(CrimeListViewModel::class.java)
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        callBacks = context as Callbacks?
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_crime_list, container, false)

        crimeRecyclerView = view.findViewById(R.id.crime_recycler_view) as RecyclerView
        crimeRecyclerView.layoutManager = LinearLayoutManager(context)
        crimeRecyclerView.adapter = adapter

        noCrimes = view.findViewById(R.id.no_crimes) as TextView
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        crimeListViewModel.crimeListLiveData.observe(
            viewLifecycleOwner,
            Observer {crimes ->
                crimes?.let {
                    Log.i(TAG, "Got Crimes ${crimes.size}")
                    updateUI(crimes)
                }
            })
    }

    override fun onDetach() {
        super.onDetach()
        callBacks = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_crime_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.new_crime -> {
                val crime = Crime()
                crimeListViewModel.addCrime(crime)
                callBacks?.onCrimeSelected(crime.id)
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun updateUI(crimes: MutableList<Crime>) {
        if (crimes.isEmpty()) {
            noCrimes.visibility = View.VISIBLE
        } else {
            noCrimes.visibility = View.GONE
        }

        adapter = CrimeAdapter(crimes)
        //adapter?.submitList(crimes) // ch 12 challenge
        crimeRecyclerView.adapter = adapter


    }

    private inner class CrimeHolder(view: View): RecyclerView.ViewHolder(view), View.OnClickListener {
        private lateinit var crime: Crime

        private val titleTextView: TextView = itemView.findViewById(R.id.crime_title)
        private val dateTextView: TextView = itemView.findViewById(R.id.crime_date)
        private val solvedImageView: ImageView = itemView.findViewById(R.id.crime_solved)

        init {
            itemView.setOnClickListener(this)
        }

        @SuppressLint("SimpleDateFormat")
        fun bind(crime:Crime) {
            this.crime = crime
            titleTextView.text = this.crime.title
            val dateFormat = SimpleDateFormat(DATE)
            dateTextView.text = dateFormat.format(this.crime.date)
            solvedImageView.visibility = if (crime.isSolved) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        override fun onClick(v: View) {
            Toast.makeText(context, "${crime.title} clicked!", Toast.LENGTH_SHORT).show()
            callBacks?.onCrimeSelected(crime.id)
        }
    }
    /*This is Chapter 12 challenge but has an issue of sumbitlist, due to outdated book*/
    //private inner class CrimeAdapter(var crimes: List<Crime>): ListAdapter<Crime, CrimeHolder>(CrimeDiffUtilCallback()) {
    private inner class CrimeAdapter(var crimes: List<Crime>): RecyclerView.Adapter<CrimeHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrimeHolder {
            /* broken with internal database
            when (viewType == 0) {
                true -> return CrimeHolder(layoutInflater.inflate(R.layout.list_item_crime_contact, parent, false))
                false -> return CrimeHolder(layoutInflater.inflate(R.layout.list_item_crime, parent, false))
            }
            */
            return CrimeHolder(layoutInflater.inflate(R.layout.list_item_crime, parent, false))
        }
        /* this is broken with internal database
        override fun getItemViewType(position: Int): Int {
            val crime = crimes[position]
            if (crime.requiresPolice) return 0
            return 1
        }
        */
        override fun getItemCount() = crimes.size

        override fun onBindViewHolder(holder: CrimeHolder, position: Int) {
            val crime = crimes[position]
            holder.bind(crime)
        }
    }

    companion object {
        fun newInstance(): CrimeListFragment {
            return CrimeListFragment()
        }
    }
}