@file:Suppress("ReplacePutWithAssignment")

package com.renatsayf.stockinsider.ui.adapters

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.renatsayf.stockinsider.R

class ExpandableMenuAdapter(private val context: Context) : BaseExpandableListAdapter()
{
    private var listDataHeader: ArrayList<String> = arrayListOf()
    private var listDataIcons: ArrayList<Drawable> = arrayListOf()
    private var listDataChild: HashMap<String, List<String>> = HashMap()

    init
    {
        listDataHeader.add(context.getString(R.string.text_title_home))
        listDataHeader.add(context.getString(R.string.text_deals_for_3_days))
        listDataHeader.add(context.getString(R.string.text_deals_for_7_days))
        listDataHeader.add(context.getString(R.string.text_deals_for_14_days))
        listDataHeader.add("Список отслеживания")
        listDataHeader.add(context.getString(R.string.text_trading_strategy))
        listDataHeader.add(context.getString(R.string.text_support_project))
        listDataHeader.add(context.getString(R.string.text_about_app))
        listDataHeader.add(context.getString(R.string.text_share))
        listDataHeader.add(context.getString(R.string.text_exit))

        // Adding data icons
        ContextCompat.getDrawable(context, R.drawable.ic_stock_hause_cold)?.let { listDataIcons.add(it) }
        ContextCompat.getDrawable(context, R.drawable.ic_calendar_3)?.let { listDataIcons.add(it) }
        ContextCompat.getDrawable(context, R.drawable.ic_calendar_7)?.let { listDataIcons.add(it) }
        ContextCompat.getDrawable(context, R.drawable.ic_calendar_14)?.let { listDataIcons.add(it) }
        ContextCompat.getDrawable(context, R.drawable.ic_visibility)?.let { listDataIcons.add(it) }
        ContextCompat.getDrawable(context, R.drawable.ic_trending_up)?.let { listDataIcons.add(it) }
        ContextCompat.getDrawable(context, R.drawable.ic_thumb_up_alt)?.let { listDataIcons.add(it) }
        ContextCompat.getDrawable(context, R.drawable.ic_info)?.let { listDataIcons.add(it) }
        ContextCompat.getDrawable(context, R.drawable.ic_share)?.let { listDataIcons.add(it) }
        ContextCompat.getDrawable(context, R.drawable.ic_power_off)?.let { listDataIcons.add(it) }

        val homeHeader: ArrayList<String> = arrayListOf()
        val latestHeader: ArrayList<String> = arrayListOf(context.getString(R.string.text_latest_purchases_1),
                                                          context.getString(R.string.text_latest_purchases_5),
                                                          context.getString(R.string.text_latest_sales_1),
                                                          context.getString(R.string.text_latest_sales_5))
        val trackingListHeader: ArrayList<String> = arrayListOf()
        val strategyHeader: ArrayList<String> = arrayListOf()
        val supportHeader: ArrayList<String> = arrayListOf(context.getString(R.string.text_financial_support),context.getString(R.string.text_watch_ads))
        val exitHeader: ArrayList<String> = arrayListOf()

        listDataChild.put(listDataHeader[0], homeHeader)
        listDataChild.put(listDataHeader[1], latestHeader)
        listDataChild.put(listDataHeader[2], latestHeader)
        listDataChild.put(listDataHeader[3], latestHeader)
        listDataChild.put(listDataHeader[4], trackingListHeader)
        listDataChild.put(listDataHeader[5], strategyHeader)
        listDataChild.put(listDataHeader[6], supportHeader)
        listDataChild.put(listDataHeader[7], arrayListOf())
        listDataChild.put(listDataHeader[8], arrayListOf())
        listDataChild.put(listDataHeader[9], exitHeader)
    }

    override fun getGroupCount(): Int
    {
        return listDataHeader.size
    }

    override fun getChildrenCount(index: Int): Int
    {
        return listDataChild[listDataHeader[index]]?.size ?: 0
    }

    override fun getGroup(index: Int): Any
    {
        return listDataHeader[index]
    }

    override fun getChild(groupIndex: Int, childIndex: Int): Any
    {
        return listDataChild[listDataHeader[groupIndex]]!![childIndex]
    }

    override fun getGroupId(index: Int): Long
    {
        return index.toLong()
    }

    override fun getChildId(p0: Int, p1: Int): Long
    {
        return p1.toLong()
    }

    override fun hasStableIds(): Boolean
    {
        return false
    }

    override fun getGroupView(groupIndex: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View
    {
        var groupView = convertView
        if (groupView == null)
        {
            groupView = LayoutInflater.from(context).inflate(R.layout.nav_group_layout, parent, false)
        }
        val iconView = groupView?.findViewById<ImageView>(R.id.iconView)
        iconView?.apply {
            setImageDrawable(listDataIcons[groupIndex])
        }
        val textView = groupView?.findViewById<TextView>(R.id.titleView)
        textView?.apply {
            text = getGroup(groupIndex) as String
        }
        val indicatorView = groupView?.findViewById<ImageView>(R.id.indicatorView)
        when {
            isExpanded && getChildrenCount(groupIndex) > 0 ->
            {
                indicatorView?.let {
                    it.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_arrow_drop_up))
                    it.visibility = View.VISIBLE
                }
            }
            !isExpanded && getChildrenCount(groupIndex) > 0 ->
            {
                indicatorView?.let {
                    it.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_arrow_drop_down))
                    it.visibility = View.VISIBLE
                }
            }
            //if the submenu is empty, then hide indicator
            getChildrenCount(groupIndex) == 0 ->
            {
                indicatorView?.visibility = View.INVISIBLE
            }
        }
        return groupView!!
    }

    override fun getChildView(groupIndex: Int, childindex: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View
    {
        val childText = getChild(groupIndex, childindex) as String
        var childView = convertView
        if (childView == null)
        {
            childView = LayoutInflater.from(context).inflate(R.layout.nav_child_layout, parent, false)
        }
        val textView = childView?.findViewById<TextView>(R.id.childView)
        textView?.text = childText

        return childView!!
    }

    override fun isChildSelectable(p0: Int, p1: Int): Boolean
    {
        return true
    }
}