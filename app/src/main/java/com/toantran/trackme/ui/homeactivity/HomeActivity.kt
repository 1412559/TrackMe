package com.toantran.trackme.ui.homeactivity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.toantran.trackme.R
import com.toantran.trackme.ui.recordsession.RecordSessionActivity
import com.toantran.trackme.ui.recordsession.RecordSessionViewModel
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {

    private lateinit var viewModel : HomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        viewModel.deleteAllTrackedLocation()

        setupRecycleView()

        setupUI();

    }

    private fun setupRecycleView() {
        val adapter = RecordSessionAdapter()
        recordList.adapter = adapter

        lifecycleScope.launch {
            viewModel.allRecordedSession.collectLatest {
                adapter.submitData(it)
            }
        }
    }

    private fun setupUI() {

        btnRecord.setOnClickListener {
            startActivity(
                Intent(this, RecordSessionActivity::class.java)
            )
        }

    }

}