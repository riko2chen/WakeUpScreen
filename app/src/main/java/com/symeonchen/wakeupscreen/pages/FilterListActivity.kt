package com.symeonchen.wakeupscreen.pages

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ToastUtils
import com.bumptech.glide.Glide
import com.symeonchen.wakeupscreen.R
import com.symeonchen.wakeupscreen.ScBaseActivity
import com.symeonchen.wakeupscreen.data.AppInfo
import com.symeonchen.wakeupscreen.data.CurrentMode
import com.symeonchen.wakeupscreen.databinding.ActivityAppFilterListBinding
import com.symeonchen.wakeupscreen.model.FilterListViewModel
import com.symeonchen.wakeupscreen.utils.UiTools

/**
 * Created by SymeonChen on 2019-10-27.
 */
class FilterListActivity : ScBaseActivity() {

    private lateinit var binding: ActivityAppFilterListBinding
    private var viewModel: FilterListViewModel? = null
    private var adapter: WhiteListViewAdapter? = null
    private val textWatcher: TextWatcher by lazy {
        object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                viewModel?.searchKey = p0?.toString() ?: ""
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppFilterListBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_app_filter_list)
        initViewModel()
        initView()
        setListener()
        setViewModelListener()
        getData()
    }

    companion object {
        const val CURRENT_MODE = "currentMode"

        fun actionStartWithMode(context: Context?, currentMode: CurrentMode) {
            context ?: return
            val intent = Intent(context, FilterListActivity::class.java)
            intent.putExtra(CURRENT_MODE, currentMode.ordinal)
            context.startActivity(intent)
        }
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(this)[FilterListViewModel::class.java]
    }

    private fun initView() {

        viewModel?.initIntent(intent)
        val currentModeValue = viewModel?.currentModeValue!!

        binding.tvTitle.text = if (currentModeValue == CurrentMode.MODE_BLACK_LIST.ordinal) {
            resources.getString(R.string.black_list)
        } else {
            resources.getString(R.string.white_list)
        }

        adapter = WhiteListViewAdapter(binding.rvAppList, mutableListOf())
        binding.rvAppList.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.rvAppList.adapter = adapter

        val hints = if (currentModeValue == CurrentMode.MODE_BLACK_LIST.ordinal) {
            resources.getString(R.string.black_list_hints)
        } else {
            resources.getString(R.string.white_list_hints)
        }
        binding.tvLogHeadHint.text = hints
    }

    private fun setListener() {
        binding.ivBack.setOnClickListener { finish() }

        binding.tvSave.setOnClickListener {
            viewModel?.saveList()
            ToastUtils.showLong(resources.getString(R.string.saved_successfully))
            finish()
        }

        binding.etSearchFilter.addTextChangedListener(textWatcher)

        binding.cbSearchSwitchSystemApp.setOnCheckedChangeListener { _, isChecked ->
            viewModel?.includeSystemApp = isChecked
        }

    }

    private fun setViewModelListener() {
        viewModel?.visibleList?.observe(this, Observer {
            if (it.size > 0) {
                UiTools.instance.hideLoading()
                adapter?.dataList?.clear()
                adapter?.dataList?.addAll(it)
                adapter?.notifyDataSetChanged()
            }
        })
    }

    private fun getData() {
        UiTools.instance.showLoading(this, binding.viewLoading)
        viewModel?.readAllApp()
    }

    override fun onDestroy() {
        try {
            binding.etSearchFilter.removeTextChangedListener(textWatcher)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onDestroy()
    }

    class WhiteListViewAdapter(recyclerView: RecyclerView, dataList: List<AppInfo>?) :
        RecyclerView.Adapter<WhiteListViewAdapter.WhiteListHolder>() {

        private var mContext: Context? = null
        var dataList: MutableList<AppInfo> = arrayListOf()

        init {
            mContext = recyclerView.context
            this.dataList.clear()
            this.dataList.addAll(dataList ?: arrayListOf())
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WhiteListHolder {
            val v = LayoutInflater.from(mContext).inflate(R.layout.item_white_list, parent, false)
            return WhiteListHolder(v)
        }

        override fun getItemCount(): Int {
            return this.dataList.size
        }

        override fun onBindViewHolder(holder: WhiteListHolder, position: Int) {
            holder.tvAppSimpleName?.text = this.dataList[position].simpleName
            holder.tvAppPackageName?.text = this.dataList[position].packageName
            holder.cbAppSelect?.isChecked = this.dataList[position].selected
            try {
                Glide.with(mContext!!)
                    .load(dataList[position].iconDrawable)
                    .into(holder.ivIcon!!)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            holder.itemView.setOnClickListener {
                this.dataList[position].selected = !this.dataList[position].selected
                notifyItemChanged(position, this.dataList[position])
            }
            holder.cbAppSelect?.setOnClickListener {
                this.dataList[position].selected = !this.dataList[position].selected
                notifyItemChanged(position, this.dataList[position])
            }
        }

        class WhiteListHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var ivIcon: ImageView? = null
            var tvAppSimpleName: TextView? = null
            var tvAppPackageName: TextView? = null
            var cbAppSelect: CheckBox? = null

            init {
                ivIcon = itemView.findViewById(R.id.iv_app_icon)
                tvAppSimpleName = itemView.findViewById(R.id.tv_app_simple_name)
                tvAppPackageName = itemView.findViewById(R.id.tv_app_package_name)
                cbAppSelect = itemView.findViewById(R.id.cb_app_select)
            }
        }

    }
}
