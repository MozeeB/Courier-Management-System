package id.cikup.couriermanagementsystem.ui.main.login

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.squareup.picasso.Picasso
import id.cikup.couriermanagementsystem.R
import id.cikup.couriermanagementsystem.data.model.BannerModel

class BannerAdapter(private val context : Context, private val bannerDomain: List<BannerModel>) : PagerAdapter() {
    private var layoutInflater : LayoutInflater? = null


    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view ===  `object`
    }

    override fun getCount(): Int {
        return bannerDomain.size
    }

    @SuppressLint("InflateParams")
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val v = layoutInflater!!.inflate(R.layout.item_banner , null)
        val image = v.findViewById<View>(R.id.imageBannerIV) as ImageView

        Picasso.get().load(bannerDomain[position].image)
            .into(image)
//        image.setBackgroundColor(Image[position])
        val vp = container as ViewPager
        vp.addView(v , 0)

        return v

    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        val vp = container as ViewPager
        val v = `object` as View
        vp.removeView(v)
    }
}