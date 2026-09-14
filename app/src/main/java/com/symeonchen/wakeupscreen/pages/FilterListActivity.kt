package com.symeonchen.wakeupscreen.pages

import com.symeonchen.wakeupscreen.services.reminder.ReminderAppSelectionController
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ToastUtils
import androidx.lifecycle.lifecycleScope
import com.symeonchen.wakeupscreen.utils.AppIconLoader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import com.symeonchen.wakeupscreen.R
import com.symeonchen.wakeupscreen.ScBaseActivity
import com.symeonchen.wakeupscreen.data.AppInfo
import com.symeonchen.wakeupscreen.data.CurrentMode
import com.symeonchen.wakeupscreen.databinding.ActivityAppFilterListBinding
import com.symeonchen.wakeupscreen.databinding.ItemWhiteListBinding
import com.symeonchen.wakeupscreen.model.FilterListViewModel
import com.symeonchen.wakeupscreen.utils.UiTools

/**
 * Created by SymeonChen on 2019-10-27.
 */
class FilterListActivity : ScBaseActivity() {

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


    private val binding by lazy { ActivityAppFilterListBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
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

        adapter = WhiteListViewAdapter(this, lifecycleScope)
        binding.rvAppList.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.rvAppList.adapter = adapter

        val hints = if (currentModeValue == CurrentMode.MODE_BLACK_LIST.ordinal) {
            resources.getString(R.string.black_list_hints)
        } else {
            resources.getString(R.string.white_list_hints)
        }
        binding.tvLogHeadHint.text = hints

        applyLightStatusBarIcons(false) // The header gradient is dark in both themes.
        applyWindowInsets()
    }

    /**
     * Edge-to-edge is enforced on Android 15+. Keep the gradient header below
     * the status bar / camera cutout, and the list above the gesture bar.
     */
    private fun applyWindowInsets() {
        val baseHeaderHeight = binding.llHeader.layoutParams.height
        val baseListPaddingBottom = binding.rvAppList.paddingBottom
        val baseLeft = binding.root.paddingLeft
        val baseRight = binding.root.paddingRight
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            binding.root.updatePadding(left = baseLeft + bars.left, right = baseRight + bars.right)
            binding.llHeader.updateLayoutParams { height = baseHeaderHeight + bars.top }
            binding.llHeader.updatePadding(top = bars.top)
            binding.rvAppList.updatePadding(bottom = baseListPaddingBottom + maxOf(bars.bottom, ime.bottom))
            insets
        }
    }

    private fun setListener() {
        binding.ivBack.setOnClickListener { finish() }

        binding.tvSave.setOnClickListener {
            viewModel?.saveList()
            ReminderAppSelectionController.onChanged(this)
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
            UiTools.instance.hideLoading()
            adapter?.submitList(it)
        })
    }

    private fun getData() {
        UiTools.instance.showLoading(this, binding.viewLoading)
        viewModel?.readAllApp()
    }

    override fun onDestroy() {
        adapter?.close()
        binding.rvAppList.adapter = null
        try {
            binding.etSearchFilter.removeTextChangedListener(textWatcher)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onDestroy()
    }

    class WhiteListViewAdapter(context: Context, parentScope: CoroutineScope) :
        RecyclerView.Adapter<WhiteListViewAdapter.WhiteListHolder>() {

        private val dataList = mutableListOf<AppInfo>()
        private val iconLoader = AppIconLoader(context)
        private val iconScope = CoroutineScope(
            parentScope.coroutineContext + SupervisorJob(parentScope.coroutineContext[Job])
        )

        fun submitList(items: List<AppInfo>) {
            dataList.clear()
            dataList.addAll(items)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WhiteListHolder =
            WhiteListHolder(ItemWhiteListBinding.inflate(LayoutInflater.from(parent.context), parent, false))

        override fun getItemCount(): Int = dataList.size

        override fun onBindViewHolder(holder: WhiteListHolder, position: Int) {
            val item = dataList[position]
            holder.binding.tvAppSimpleName.text = item.simpleName
            holder.binding.tvAppPackageName.text = item.packageName
            holder.binding.cbAppSelect.isChecked = item.selected
            holder.iconJob?.cancel()
            holder.binding.ivAppIcon.setImageDrawable(null)
            // The XML defines the actual display size, including device density.
            val sizePx = holder.binding.ivAppIcon.layoutParams.width.coerceAtLeast(1)
            holder.iconJob = iconScope.launch {
                val bitmap = iconLoader.load(item.packageName, sizePx)
                holder.binding.ivAppIcon.setImageBitmap(bitmap)
            }
            val toggle = {
                val currentPosition = holder.bindingAdapterPosition
                if (currentPosition != RecyclerView.NO_POSITION) {
                    val currentItem = dataList[currentPosition]
                    currentItem.selected = !currentItem.selected
                    holder.binding.cbAppSelect.isChecked = currentItem.selected
                }
            }
            holder.itemView.setOnClickListener { toggle() }
            holder.binding.cbAppSelect.setOnClickListener { toggle() }
        }

        override fun onViewRecycled(holder: WhiteListHolder) {
            holder.iconJob?.cancel()
            holder.iconJob = null
            holder.binding.ivAppIcon.setImageDrawable(null)
            super.onViewRecycled(holder)
        }

        fun close() {
            iconScope.cancel()
            iconLoader.close()
            dataList.clear()
        }

        class WhiteListHolder(val binding: ItemWhiteListBinding) : RecyclerView.ViewHolder(binding.root) {
            var iconJob: Job? = null
        }
    }
}
