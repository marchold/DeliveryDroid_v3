package catglo.com.deliverydroid.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import catglo.com.deliverydroid.R
import org.joda.time.DateTime
import org.joda.time.MutableDateTime
import org.joda.time.format.DateTimeFormat

class WeekdayCell(context: Context): RecyclerView.ViewHolder(
    View.inflate(
        context,
        R.layout.weekday_picker_cell,
        null
    )
){
    val textView = itemView.findViewById<TextView>(R.id.textView)
    val subtextView = itemView.findViewById<TextView>(R.id.relativeDay)
}

class HorizontalWeekdayList(val context: Context, val date : MutableDateTime):
    RecyclerView.Adapter<WeekdayCell>(){

    val currentWeekdayIndex = (if (date.dayOfWeek().get()==0) 7 else date.dayOfWeek().get())-1
    var relativeWeekdayIndex = (if (date.dayOfWeek().get()==0) 7 else date.dayOfWeek().get())-1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeekdayCell {
        return WeekdayCell(context)
    }

    override fun getItemCount(): Int {
        return 14
    }

    override fun onBindViewHolder(holder: WeekdayCell, position: Int) {
        holder.textView.text =  when (position) {
            1 ->  { context.getString(R.string.Tuesday)  }
            2 ->  { context.getString(R.string.Wendsday) }
            3 ->  { context.getString(R.string.Thursday) }
            4 ->  { context.getString(R.string.Friday)   }
            5 ->  { context.getString(R.string.Saturday) }
            6 ->  { context.getString(R.string.Sunday)   }
            7 ->  { context.getString(R.string.Monday)   }
            8 ->  { context.getString(R.string.Tuesday)  }
            9 ->  { context.getString(R.string.Wendsday) }
            10 -> { context.getString(R.string.Thursday) }
            11 -> { context.getString(R.string.Friday)   }
            12 -> { context.getString(R.string.Saturday) }
            13 -> { context.getString(R.string.Sunday)   }
            else->{ context.getString(R.string.Monday)   }
        }
        val relativeWeekdayIndex = position-currentWeekdayIndex
        holder.subtextView.text = when (relativeWeekdayIndex) {
            -1 -> { "yesterday" }
            0  -> { "today" }
            1  -> { "tomorrow" }
            else -> {
                DateTimeFormat.mediumDate()
                    .print(DateTime(date).plusDays(relativeWeekdayIndex))
            }
        }
        holder.itemView.tag = relativeWeekdayIndex
    }
}

class HorizontalDatePicker @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : RecyclerView(context, attrs, defStyleAttr)
{
    val linearLayoutManager = LinearLayoutManager(
        context,
        LinearLayoutManager.HORIZONTAL,
        false
    )
    val helper = LinearSnapHelper()
    var weekdayAdapter : HorizontalWeekdayList? = null
    var dateTime : MutableDateTime? = null

    var onDaySelected : ((dayOffet:Int)->Unit)? = null

    init {
        this.layoutManager = linearLayoutManager
        helper.attachToRecyclerView(this)
    }

    fun setTime(time: MutableDateTime) {
        if (this.dateTime?.dayOfYear ?: -1 != time.dayOfYear){
            weekdayAdapter = HorizontalWeekdayList(context, MutableDateTime(time))
            adapter = weekdayAdapter
            addOnScrollListener(object: OnScrollListener(){
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    (helper.findSnapView(linearLayoutManager)?.tag as? Int)?.let { dayOffset ->
                        onDaySelected?.let { it(dayOffset) }
                        weekdayAdapter?.relativeWeekdayIndex = dayOffset-1
                    }
                }
            })
            snap()
        }
        this.dateTime = time
    }

    fun snap(){
        (adapter as? HorizontalWeekdayList)?.let { weekdayAdapter ->
            linearLayoutManager.scrollToPositionWithOffset(weekdayAdapter.currentWeekdayIndex, 0)
            post {
                helper.findSnapView(linearLayoutManager)?.let { view ->
                    helper.calculateDistanceToFinalSnap(linearLayoutManager, view)?.let {
                        if (it[0] != 0 || it[1] != 0) {
                            smoothScrollBy(it[0], it[1])
                        }
                    }
                }
            }
        }
    }
}