package com.model01.molecule

import android.app.Activity
import android.os.Bundle
import android.graphics.*
import android.view.*
import android.widget.*
import kotlin.math.*

data class Atom(val element:String,val x:Float,val y:Float,val z:Float)
data class Bond(val a:Int,val b:Int)
data class Molecule(val name:String,val formula:String,val atoms:List<Atom>,val bonds:List<Bond>)

class MainActivity:Activity(){
    private lateinit var viewer:MoleculeView
    private val samples=listOf(
        Molecule("水","H₂O",listOf(Atom("O",0f,0f,0f),Atom("H",-0.8f,0.55f,0f),Atom("H",0.8f,0.55f,0f)),listOf(Bond(0,1),Bond(0,2))),
        Molecule("甲烷","CH₄",listOf(Atom("C",0f,0f,0f),Atom("H",0.8f,0.8f,0.8f),Atom("H",-0.8f,-0.8f,0.8f),Atom("H",-0.8f,0.8f,-0.8f),Atom("H",0.8f,-0.8f,-0.8f)),listOf(Bond(0,1),Bond(0,2),Bond(0,3),Bond(0,4))),
        Molecule("二氧化碳","CO₂",listOf(Atom("O",-1.2f,0f,0f),Atom("C",0f,0f,0f),Atom("O",1.2f,0f,0f)),listOf(Bond(0,1),Bond(1,2))),
        Molecule("氨","NH₃",listOf(Atom("N",0f,0f,0f),Atom("H",0.9f,0.2f,0.3f),Atom("H",-0.45f,0.8f,0.3f),Atom("H",-0.45f,-0.7f,0.3f)),listOf(Bond(0,1),Bond(0,2),Bond(0,3))),
        Molecule("苯","C₆H₆", (0..11).map{ i-> val r=if(i<6) 1f else 1.65f; val a=2*PI*(i%6)/6; Atom(if(i<6)"C" else "H",(r*cos(a)).toFloat(),(r*sin(a)).toFloat(),0f) }, (0..5).map{Bond(it,(it+1)%6)}+ (0..5).map{Bond(it,it+6)})
    )
    override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState)
        window.statusBarColor=Color.rgb(13,22,39)
        val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setBackgroundColor(Color.rgb(13,22,39));setPadding(14,12,14,8)}
        val title=TextView(this).apply{text="MODEL01 · 三维分子模型";textSize=21f;setTextColor(Color.WHITE);gravity=Gravity.CENTER;setPadding(0,8,0,12)}
        root.addView(title)
        val spinner=Spinner(this)
        val adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,samples.map{"${it.name}  ${it.formula}"})
        spinner.adapter=adapter
        root.addView(spinner)
        viewer=MoleculeView(this,samples[0])
        root.addView(viewer,LinearLayout.LayoutParams(-1,0,1f))
        val info=TextView(this).apply{text="拖动旋转 · 双指缩放";textSize=13f;setTextColor(Color.LTGRAY);gravity=Gravity.CENTER;setPadding(0,0,0,8)}
        root.addView(info)
        val row=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER}
        fun button(label:String,action:()->Unit){row.addView(Button(this).apply{text=label;setOnClickListener{action()}},LinearLayout.LayoutParams(0,52,1f))}
        button("标注 / 净图"){viewer.labels=!viewer.labels;viewer.invalidate()}
        button("球棍"){viewer.style=0;viewer.invalidate()}
        button("空间填充"){viewer.style=1;viewer.invalidate()}
        button("复位"){viewer.reset();viewer.invalidate()}
        root.addView(row)
        spinner.onItemSelectedListener=object:AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(p:AdapterView<*>?){}
            override fun onItemSelected(p:AdapterView<*>?,v:View?,pos:Int,id:Long){viewer.molecule=samples[pos];viewer.reset();viewer.invalidate()}
        }
        setContentView(root)
    }
}
class MoleculeView(context:android.content.Context,var molecule:Molecule):View(context){
    var labels=true;var style=0;private var rx=-0.35f;private var ry=0.35f;private var scale=1f
    private val atomPaint=Paint(3);private val bondPaint=Paint(3).apply{color=Color.LTGRAY;strokeWidth=9f;strokeCap=Paint.Cap.ROUND}
    private val textPaint=Paint(3).apply{color=Color.WHITE;textSize=30f;textAlign=Paint.Align.CENTER;typeface=Typeface.DEFAULT_BOLD}
    private var lastX=0f;private var lastY=0f;private var oldDist=0f
    fun reset(){rx=-0.35f;ry=0.35f;scale=1f}
    override fun onDraw(c:Canvas){super.onDraw(c);c.drawColor(Color.rgb(13,22,39))
        val cx=width/2f;val cy=height/2f
        val projected=molecule.atoms.map{a->
            val y1=a.y*cos(rx)-a.z*sin(rx);val z1=a.y*sin(rx)+a.z*cos(rx)
            val x2=a.x*cos(ry)+z1*sin(ry);val z2=-a.x*sin(ry)+z1*cos(ry)
            val f=scale*min(width,height)*0.22f/(1f+z2*0.12f)
            floatArrayOf(cx+x2*f,cy-y1*f,z2)
        }
        molecule.bonds.forEach{b->val p=projected[b.a];val q=projected[b.b];bondPaint.strokeWidth=if(style==1)18f else 8f;c.drawLine(p[0],p[1],q[0],q[1],bondPaint)}
        molecule.atoms.indices.sortedBy{projected[it][2]}.forEach{i->
            val p=projected[i];val e=molecule.atoms[i].element
            val color=when(e){"C"->Color.DKGRAY;"O"->Color.rgb(235,65,65);"N"->Color.rgb(65,110,245);"H"->Color.WHITE;"S"->Color.YELLOW;"P"->Color.rgb(240,145,35);else->Color.GREEN}
            val radius=(if(style==1)34f else if(e=="H")17f else 25f)*scale.coerceIn(.65f,1.7f)
            atomPaint.color=color;c.drawCircle(p[0],p[1],radius,atomPaint)
            atomPaint.color=Color.argb(90,255,255,255);c.drawCircle(p[0]-radius*.3f,p[1]-radius*.35f,radius*.25f,atomPaint)
            if(labels){textPaint.textSize=if(style==1)20f else 25f;c.drawText(e,p[0],p[1]-radius-8f,textPaint)}
        }
        textPaint.textSize=24f;textPaint.color=Color.LTGRAY;c.drawText("${molecule.name}  ${molecule.formula}",cx,height-28f,textPaint);textPaint.color=Color.WHITE
    }
    override fun onTouchEvent(e:android.view.MotionEvent):Boolean{
        when(e.actionMasked){
            MotionEvent.ACTION_DOWN->{lastX=e.x;lastY=e.y;return true}
            MotionEvent.ACTION_POINTER_DOWN->{if(e.pointerCount>=2)oldDist=distance(e);return true}
            MotionEvent.ACTION_MOVE->{if(e.pointerCount>=2){val d=distance(e);if(oldDist>0)scale=(scale*d/oldDist).coerceIn(.45f,3.5f);oldDist=d}
                else{ry+=(e.x-lastX)*.009f;rx+=(e.y-lastY)*.009f};lastX=e.x;lastY=e.y;invalidate();return true}
            MotionEvent.ACTION_UP,MotionEvent.ACTION_POINTER_UP->{oldDist=0f;return true}
        };return true
    }
    private fun distance(e:MotionEvent)=hypot(e.getX(0)-e.getX(1),e.getY(0)-e.getY(1))
}
