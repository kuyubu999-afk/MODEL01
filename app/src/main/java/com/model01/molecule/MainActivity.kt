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
    private lateinit var input:EditText
    private lateinit var info:TextView
    private val samples=listOf(
        Molecule("水","H₂O",listOf(Atom("O",0f,0f,0f),Atom("H",-0.8f,0.55f,0f),Atom("H",0.8f,0.55f,0f)),listOf(Bond(0,1),Bond(0,2))),
        Molecule("甲烷","CH₄",listOf(Atom("C",0f,0f,0f),Atom("H",0.8f,0.8f,0.8f),Atom("H",-0.8f,-0.8f,0.8f),Atom("H",-0.8f,0.8f,-0.8f),Atom("H",0.8f,-0.8f,-0.8f)),listOf(Bond(0,1),Bond(0,2),Bond(0,3),Bond(0,4))),
        Molecule("二氧化碳","CO₂",listOf(Atom("O",-1.2f,0f,0f),Atom("C",0f,0f,0f),Atom("O",1.2f,0f,0f)),listOf(Bond(0,1),Bond(1,2))),
        Molecule("氨","NH₃",listOf(Atom("N",0f,0f,0f),Atom("H",0.9f,0.2f,0.3f),Atom("H",-0.45f,0.8f,0.3f),Atom("H",-0.45f,-0.7f,0.3f)),listOf(Bond(0,1),Bond(0,2),Bond(0,3))),
        Molecule("苯","C₆H₆",(0..11).map{i->val r=if(i<6)1f else 1.65f;val a=2*PI*(i%6)/6;Atom(if(i<6)"C" else "H",(r*cos(a)).toFloat(),(r*sin(a)).toFloat(),0f)},(0..5).map{Bond(it,(it+1)%6)}+(0..5).map{Bond(it,it+6)}),
        Molecule("氧气","O₂",listOf(Atom("O",-0.65f,0f,0f),Atom("O",0.65f,0f,0f)),listOf(Bond(0,1))),
        Molecule("氮气","N₂",listOf(Atom("N",-0.65f,0f,0f),Atom("N",0.65f,0f,0f)),listOf(Bond(0,1))),
        Molecule("乙醇","C₂H₆O",listOf(Atom("C",-0.7f,0f,0f),Atom("C",0.5f,0.2f,0f),Atom("O",1.5f,-0.3f,0f),Atom("H",-1.1f,0.9f,0.5f),Atom("H",-1.1f,-0.8f,-0.5f),Atom("H",-0.9f,-0.5f,0.9f),Atom("H",0.4f,1.2f,0.5f),Atom("H",0.4f,-0.8f,-0.5f),Atom("H",2.1f,0.1f,0.2f)),listOf(Bond(0,1),Bond(1,2),Bond(0,3),Bond(0,4),Bond(0,5),Bond(1,6),Bond(1,7),Bond(2,8))),
        Molecule("过氧化氢","H₂O₂",listOf(Atom("O",-0.65f,0f,0f),Atom("O",0.65f,0f,0.3f),Atom("H",-1.2f,0.7f,0f),Atom("H",1.2f,-0.5f,0.4f)),listOf(Bond(0,1),Bond(0,2),Bond(1,3))),
        Molecule("二氧化硫","SO₂",listOf(Atom("S",0f,0f,0f),Atom("O",-0.9f,0.7f,0f),Atom("O",0.9f,0.7f,0f)),listOf(Bond(0,1),Bond(0,2)))
    )

    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState)
        val bg=Color.rgb(13,22,39)
        val panel=Color.rgb(25,38,59)
        val accent=Color.rgb(54,125,220)
        window.statusBarColor=bg
        window.navigationBarColor=bg

        fun rounded(color:Int)=android.graphics.drawable.GradientDrawable().apply{
            setColor(color);cornerRadius=12f
        }
        val root=LinearLayout(this).apply{
            orientation=LinearLayout.VERTICAL
            setBackgroundColor(bg)
            setPadding(14,8,14,8)
        }
        val title=TextView(this).apply{
            text="MODEL01 · 三维分子模型"
            textSize=22f;setTextColor(Color.WHITE)
            gravity=Gravity.CENTER;setPadding(0,8,0,12)
        }
        root.addView(title,LinearLayout.LayoutParams(-1,-2))

        val inputRow=LinearLayout(this).apply{
            orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL
        }
        input=EditText(this).apply{
            hint="输入名称或分子式，如：水、乙醇、C6H6"
            setHintTextColor(Color.LTGRAY);setTextColor(Color.WHITE)
            textSize=15f;setSingleLine(true);imeOptions=android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH
            setPadding(12,0,12,0);background=rounded(panel)
        }
        inputRow.addView(input,LinearLayout.LayoutParams(0,52,1f))
        val search=Button(this).apply{
            text="生成模型";textSize=14f;setTextColor(Color.WHITE)
            background=rounded(accent);isAllCaps=false
            setOnClickListener{loadInput()}
        }
        inputRow.addView(search,LinearLayout.LayoutParams(112,52).apply{leftMargin=8})
        root.addView(inputRow,LinearLayout.LayoutParams(-1,-2))

        val sampleLabel=TextView(this).apply{
            text="示例分子";textSize=13f;setTextColor(Color.LTGRAY)
            setPadding(2,10,0,4)
        }
        root.addView(sampleLabel)
        val spinner=Spinner(this).apply{
            background=rounded(panel);setPadding(8,0,8,0)
        }
        val names=samples.map{"${it.name}  ${it.formula}"}
        val adapter=object: ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,names){
            override fun getView(position:Int,convertView:View?,parent:android.view.ViewGroup):View{
                val v=super.getView(position,convertView,parent) as TextView
                v.setTextColor(Color.WHITE);v.textSize=15f;v.setPadding(10,8,8,8);return v
            }
            override fun getDropDownView(position:Int,convertView:View?,parent:android.view.ViewGroup):View{
                val v=super.getDropDownView(position,convertView,parent) as TextView
                v.setTextColor(Color.WHITE);v.setBackgroundColor(panel);v.setPadding(16,14,8,14);return v
            }
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter=adapter
        root.addView(spinner,LinearLayout.LayoutParams(-1,48))

        viewer=MoleculeView(this,samples[0])
        root.addView(viewer,LinearLayout.LayoutParams(-1,0,1f))
        info=TextView(this).apply{
            text="已载入水分子 · 拖动旋转，双指缩放"
            textSize=12f;setTextColor(Color.LTGRAY)
            gravity=Gravity.CENTER;setPadding(0,4,0,8)
        }
        root.addView(info,LinearLayout.LayoutParams(-1,-2))

        val row=LinearLayout(this).apply{
            orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER
        }
        fun button(label:String,action:()->Unit){
            val b=Button(this).apply{
                text=label;setTextSize(11f);setTextColor(Color.WHITE)
                isAllCaps=false;background=rounded(panel);setOnClickListener{action()}
            }
            row.addView(b,LinearLayout.LayoutParams(0,46,1f).apply{setMargins(2,0,2,0)})
        }
        button("标注 / 净图"){viewer.labels=!viewer.labels;viewer.invalidate()}
        button("球棍"){viewer.style=0;viewer.invalidate()}
        button("空间填充"){viewer.style=1;viewer.invalidate()}
        button("复位"){viewer.reset();viewer.invalidate()}
        root.addView(row,LinearLayout.LayoutParams(-1,48))

        spinner.onItemSelectedListener=object:AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(p:AdapterView<*>?){}
            override fun onItemSelected(p:AdapterView<*>?,v:View?,pos:Int,id:Long){
                viewer.molecule=samples[pos];input.setText(samples[pos].name)
                info.text="已载入示例：${samples[pos].name}"
                viewer.reset();viewer.invalidate()
            }
        }
        input.setOnEditorActionListener{_,_,_->loadInput();true}
        setContentView(root)
    }

    private fun loadInput(){
        val query=input.text.toString().trim()
        if(query.isEmpty()){info.text="请先输入化合物名称或分子式";return}
        val normalized=query.replace("₂","2").replace("₃","3").replace("₄","4").replace("₅","5").replace("₆","6").replace("₇","7").replace("₈","8").replace("₉","9").replace("₀","0").replace(" ","")
        val found=samples.firstOrNull{it.name.equals(query,true)||it.formula.replace("₂","2").replace("₃","3").replace("₄","4").replace("₆","6").equals(normalized,true)}
        if(found!=null){viewer.molecule=found;info.text="已载入：${found.name}（内置结构）";viewer.reset();viewer.invalidate();return}
        val parsed=parseFormula(normalized)
        if(parsed==null){info.text="暂未识别。可输入：水、甲烷、二氧化碳、氨、苯、氧气、氮气、乙醇、过氧化氢、二氧化硫，或有效分子式。";return}
        viewer.molecule=makeSchematic(normalized,parsed)
        info.text="分子式示意：原子数量正确，但键连接和空间构型未经化学结构库验证。"
        viewer.reset();viewer.invalidate()
    }

    private fun parseFormula(formula:String):List<String>?{
        if(formula.isEmpty()||formula.length>40)return null
        val regex=Regex("([A-Z][a-z]?)([0-9]*)")
        val out=mutableListOf<String>();var cursor=0
        for(m in regex.findAll(formula)){
            if(m.range.first!=cursor)return null
            val symbol=m.groupValues[1];val count=m.groupValues[2].ifEmpty{"1"}.toIntOrNull()?:return null
            if(symbol !in setOf("H","C","N","O","F","P","S","Cl","Br","I","Na","K","Ca","Mg"))return null
            if(count !in 1..30||out.size+count>100)return null
            repeat(count){out.add(symbol)}
            cursor=m.range.last+1
        }
        return if(cursor==formula.length&&out.isNotEmpty())out else null
    }

    private fun makeSchematic(formula:String,elements:List<String>):Molecule{
        val atoms=elements.mapIndexed{i,e->
            val angle=i*2.399963f
            val radius=0.45f+sqrt(i.toFloat())*0.48f
            Atom(e,cos(angle)*radius,sin(angle)*radius,((i%3)-1)*0.28f)
        }
        val bonds=(0 until elements.size-1).map{Bond(it,it+1)}
        return Molecule("分子式示意",formula,atoms,bonds)
    }
}

class MoleculeView(context:android.content.Context,var molecule:Molecule):View(context){
    var labels=true;var style=0;private var rx=-0.35f;private var ry=0.35f;private var scale=1f
    private val atomPaint=Paint(3);private val bondPaint=Paint(3).apply{color=Color.LTGRAY;strokeWidth=9f;strokeCap=Paint.Cap.ROUND}
    private val textPaint=Paint(3).apply{color=Color.WHITE;textSize=30f;textAlign=Paint.Align.CENTER;typeface=Typeface.DEFAULT_BOLD}
    private var lastX=0f;private var lastY=0f;private var oldDist=0f
    fun reset(){rx=-0.35f;ry=0.35f;scale=1f}
    override fun onDraw(c:Canvas){
        super.onDraw(c);c.drawColor(Color.rgb(13,22,39))
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
    override fun onTouchEvent(e:MotionEvent):Boolean{
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
