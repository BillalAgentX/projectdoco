package com.projectdocupro.mobile.fragments.add_direction;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.Display;
import android.view.ScaleGestureDetector;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.TextView;

import com.projectdocupro.mobile.activities.HomeActivity;
import com.projectdocupro.mobile.utility.Utils;

/**
 * Erweiterung der Klasse WebView zur Anzeige der Pl&auml;ne in der Klasse ProjectDocuShowPlanFragment
 *
 * @see    WebView
 * @see ProjectDocuShowPlanFragment
 */
public class EnhancedWebView extends WebView {
    public float touchPosX = -1;
    public float touchPosY = -1;

    public float webviewWidth = -1;
    public float webviewHeight = -1;

    public float currentScrollPositionX = -1;
    public float currentScrollPositionY = -1;

    public float oldScrollPositionX = -1;
    public float oldScrollPositionY = -1;

    public float testScaleValue = 350;

    public float scaleFactor = 1;
    public float oldScaleFactor = 1;

    public float oldActionBarOffset = 0;

    public float crosshairPositionX = 0;
    public float crosshairPositionY = 0;

    public float oldCrosshairPositionX = 0;
    public float oldCrosshairPositionY = 0;

    public float scaledCrosshairPositionX = 0;
    public float scaledCrosshairPositionY = 0;

    public TextView debugTextView2 = null;
    public TextView debugTextView3 = null;
    public TextView debugTextView4 = null;

    public float planResizeFactor = 1.0f;
    public float resizedPlanWidth = -1.0f;
    public float resizedPlanHeight = -1.0f;

    public float rightMargin = -1.0f;
    public float leftMargin = -1.0f;
    public float topMargin = -1.0f;
    public float bottomMargin = -1.0f;

    public float displayWidth = -1.0f;
    public float displayHeight = -1.0f;

    public float planWidth = -1;
    public float planHeight = -1;

    public double arrowRotationAngle = 0;
    public double oldRotationAngle = 0;
    public float viewX = 0.0f;
    public float viewY = 100.0f;

    public float oldViewX = 0.0f;
    public float oldViewY = 100.0f;

    public int oldMarginWidth = -1;
    public int oldMarginHeight = -1;

    public boolean noMoreScroll = false;
    public boolean scrollingOn = true;
    public boolean rotatingArrowLocation = false;
    public boolean rotatingPlanLocation = false;
    public boolean crosshairIsOutsidePlan = false;

    private String webviewHtml = null;

    boolean isScaleFactorSet = false;

    private Context context = null;
    private ScaleGestureDetector myScaleDetector = null;

    public EnhancedWebView(Context context) {
        super(context);
        this.context = context;

        if (!isInEditMode()) {
            init();
        }
    }

    public EnhancedWebView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        this.context = context;

        if (!isInEditMode()) {
            init();
        }
    }

    public EnhancedWebView(Context context, AttributeSet attrs, int defaultStyle) {
        super(context, attrs, defaultStyle);
        this.context = context;

        if (!isInEditMode()) {
            init();
        }
    }

    public void init() {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        int statusBarHeight = getStatusBarHeight();


        //Utils.showLogger("statusBarHeight>>"+statusBarHeight);

        Point size = new Point();
        display.getSize(size);

        this.displayWidth = size.x;
        this.displayHeight = size.y - statusBarHeight;

        //enhcnge
        //System.out.println("#### size-x:"+size.x+" size-y:"+size.y+ " statusbarHeight:"+statusBarHeight);

    }

    public int getWebviewWidth() {
        return this.computeHorizontalScrollRange();
    }

    public int getWebviewHeight() {
        return this.computeVerticalScrollRange();
    }

    /**
     * Methode zum Erzeugen eines HTML-Codes zur Anzeige eines Plans
     *
     * @param imagePath Pfad zum anzuzeigenden Plan
     * @return String mit HTML-Code zur Anzeige des Plans im WebView
     */
    public String createHTMLPlan(String imagePath) {
        //System.out.println    ("#### resizedPlanWidth:"+resizedPlanWidth+ " resizedPlanHeight:"+resizedPlanHeight+ " leftMargin:"+leftMargin+ " rightMargin:"+rightMargin);
        String html = "";
        html += "<html>";
        html += "<head>";
        html += "<meta name='viewport' content='width=" + resizedPlanWidth + ", initial-scale=1.0'>";
        html += "<link rel='stylesheet' type='text/css' href='pdstyle.css'>";
        html += "<script>";
        html += "function setTableMargin (marginWidth, marginHeight) {";
        html += "var planTable=document.getElementById('planTable');";
        html += "var marginA=document.getElementById('marginA');";
        html += "var marginB=document.getElementById('marginB');";
        html += "var marginC=document.getElementById('marginC');";
        html += "var marginD=document.getElementById('marginD');";
        html += "planTable.width=(marginWidth+" + resizedPlanWidth + "+marginWidth)+'px';";
        html += "marginA.width=marginWidth+'px';";
        html += "marginA.height=marginHeight+'px';";
        html += "marginB.width=marginWidth+'px';";
        html += "marginC.height=marginHeight+'px';";
        html += "marginD.width=marginWidth+'px';";
        html += "}";
        html += "</script>";
        html += "</head>";
        html += "<body style='zoom:100%;'>";
        html += "<table id='planTable' class='pdtable' width='" + (leftMargin + resizedPlanWidth + rightMargin) + "px'>";
       // html += "<table id='planTable' class='pdtable' height='" + (topMargin + resizedPlanHeight + bottomMargin) + "px'>";

        // Top Table
        html += "<tr><td id='marginA' width='" + leftMargin + "px' height='" + topMargin + "px'>&nbsp;</td>";
        html += "<td>&nbsp;</td>";
        html += "<td id='marginB' width='" + rightMargin + "px' >&nbsp;</td></tr>";

        // Middle Table
        html += "<tr><td height='" + resizedPlanHeight + "px'>&nbsp;</td>";
        html += "<td width='" + resizedPlanWidth + "px' height='" + resizedPlanHeight + "px'><img src='" + imagePath + "' width='" + resizedPlanWidth + "px' height='" + resizedPlanHeight + "px'></td>";
        html += "<td>&nbsp;</td></tr>";

        // Bottom Table
        html += "<tr><td id='marginC' height='" + bottomMargin + "px'>&nbsp;</td>";
        html += "<td id='marginD' width=" + leftMargin + "px>&nbsp;</td>";
        html += "<td>&nbsp;</td></tr>";

        // Ende von Table und Html
        html += "</table>";
        html += "</body>";
        html += "</html>";

        return html;
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, 0, 0);



        //super.onSizeChanged(w,h,0,0);
        System.out.println("##### before w:" + w + " h:" + h + " scaling:" + this.getScaleX() + " --- " + this.getScaleY() + "scalefactor:" + this.scaleFactor + " WbViewGetHeight:" + this.getHeight() + " --- " + this.getContentHeight() + " scalex:" + this.scaledCrosshairPositionX + " scaley:" + this.scaledCrosshairPositionY);
//		int realDisplayHeight = ProjectDocuMainActivity.projectDocuMainActivity.getWindow().getDecorView().getHeight();
//		int realDisplayWidth = ProjectDocuMainActivity.projectDocuMainActivity.getWindow().getDecorView().getWidth();
       // int statusBarHeight = getStatusBarHeight();

        // https://stackoverflow.com/questions/4610715/how-to-determine-if-a-webview-is-zoomed-out-all-the-way

        // landscape mode von vorn herein
        // => oldw und oldh leer, w = 1920 h = 1005
        //
        // von portrait kommend
        // => w = 1920 h = 1005 oldw = 1080 oldh = 1845^
        //super.onSizeChanged(1080, 1845, 0, 0);
		
		/*System.out.println("ProjectDocuWebview w:"+w+" - h:"+h+" - oldw:"+oldw+" - oldh:"+oldh);
		System.out.println("ProjectDocuWebview real height:"+ProjectDocuMainActivity.projectDocuMainActivity.getWindow().getDecorView().getHeight());
		System.out.println("ProjectDocuWebview real width:"+ProjectDocuMainActivity.projectDocuMainActivity.getWindow().getDecorView().getWidth());
		System.out.println("ProjectDocuWebview statusbar-height:"+getStatusBarHeight());
			*/


        // coming from portrait mode
/*		if (oldw != 0 && oldh != 0){
			//System.out.println("ProjectDocuWebview coming from portrait"+oldw+"---"+oldh);
			w = oldw;
			h = oldh;
		}
		// coming from landscape mode
		else{
			//System.out.println("ProjectDocuWebview coming from landscape:"+oldw+"---"+oldw);
		}*/
      //  w = h + statusBarHeight;
       // h = w - statusBarHeight;

        //System.out.println("##### webview scaling: "+this.getScaleX() + " - "+this.getScaleY()+ " - " +this.scaledCrosshairPositionX + " - " +this.scaledCrosshairPositionY + " - " +this.scaleFactor  + " - " +this.getScale() + " - " + getResources().getDisplayMetrics().density);
        //System.out.println("##### after w:" + w + " h:"+h + this.planResizeFactor);
        //super.onSizeChanged(w,h,0,0);

        // System.out.println("this.displayHeight:"+this.displayHeight+" this.displayWidth:"+this.displayWidth);
        // System.out.println("onSizeChanged: w h oldw oldh: "+w+" - "+h+" - "+oldw+" - "+oldh);

    }

    /**
     * Returns the height of the Status-Bar
     *
     * @return
     */
    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * Methode zum Berechnen der Gr&ouml;&szlig;e des Anzeigebereiches f&uuml;r den Plan
     *
     * @param scaleWidth  Breite
     * @param scaleHeight H&ouml;he
     * @param scale       Skalierung des Plans
     */
    public void calculateSizes(int scaleWidth, int scaleHeight, boolean scale) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        Point size = new Point();
        display.getSize(size);

        this.planResizeFactor = -1;

        if (scaleWidth < scaleHeight) {
            this.planResizeFactor = this.planHeight / this.displayHeight;
            //3537/2308
        } else {
            this.planResizeFactor = this.planWidth / this.displayWidth;
        }


        if (!scale) {
            this.planResizeFactor = 1;
        }

        this.resizedPlanWidth = (this.resizedPlanWidth * this.scaleFactor);
        this.resizedPlanHeight = (this.resizedPlanHeight * this.scaleFactor);

        this.rightMargin = this.displayWidth / 2;
        this.leftMargin = this.displayWidth / 2;
        this.topMargin = this.displayHeight / 2;
        this.bottomMargin = this.displayHeight / 2;

        oldMarginWidth = Math.round(leftMargin);
        oldMarginHeight = Math.round(topMargin);
    }


    public float defaultMarginWidth = this.displayWidth / 2;
    public float defaultMarginHeight = this.displayHeight / 2;


    @Override
    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    protected void onScrollChanged(final int l, final int t, final int oldl, final int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);

//		ProjectDocuDatabaseManager projectDocuDatabaseManager = new ProjectDocuDatabaseManager(context);
//		projectDocuDatabasonScroeManager.updatePreferences(Pr   ojectDocuDatabaseManager.COLUMN_PREFERENCES_IS_LOCATED, 1);
//		projectDocuDatabaseManager.updatePreferences(ProjectDocuDatabaseManager.COLUMN_PREFERENCES_CURRENT_SCALE, getScale());
//		ProjectDocuShowPlanFragment.projectDocuShowPlanFragment.setLocatedPlanIcon();
        HomeActivity.flags.is_arrow_located = 1;

        HomeActivity.flags.scale_factor = getScale();

        //Utils.showLogger("scrollY"+this.getScrollY());

        touchPosX = this.getScrollX() - this.displayWidth / 2;
        touchPosY = this.getScrollY() - this.displayHeight / 2;


        float cusScaleF = getContentHeight() / (planHeight + topMargin + bottomMargin);

        //Utils.showLogger("myScaleFactor>>"+cusScaleF);
       // Utils.showLogger("oldScaleFactor>>"+getScale());

       //Utils.showLogger("contentHeight>>>"+getContentHeight());
       // Utils.showLogger("displayHeight>>>"+displayHeight);

    //    Utils.showLogger("actualHeightWeb>>"+(planHeight+topMargin+bottomMargin));
      // Utils.showLogger(
        // "calcHeight>>>"+((planHeight+topMargin+bottomMargin)*scaleFactor));
        if(!this.isScaleFactorSet)
        this.scaleFactor = this.getScale();

//		this.defaultMarginWidth = this.displayWidth / 2;
//		this.defaultMarginHeight = this.displayHeight / 2;

        this.defaultMarginWidth = this.displayWidth / 2;
        this.defaultMarginHeight = this.displayHeight / 2;

        if (getScale() != oldScaleFactor) {
            //System.out.println("#### oldScaleFactor:" + oldScaleFactor + " --- " + this.scaleFactor);
            if (android.os.Build.VERSION.SDK_INT < 19) {
                this.loadUrl("javascript:setTableMargin(" + Math.round(defaultMarginWidth / scaleFactor) + "," + Math.round(defaultMarginHeight / scaleFactor) + ");");
            } else {
                //System.out.println("hier !!!");
                System.out.println("##### debugText0: leftMagrin:" + leftMargin + " -resizdePlanWidth -- " + resizedPlanWidth + "rightMargin:" + rightMargin + "defaultMarginWidth:" + defaultMarginWidth + "scaleFactor:" + scaleFactor);
                //leftMargin = leftMargin / this.scaleFactor;
                //rightMargin = rightMargin / this.scaleFactor;

                // old position correction
                //this.evaluateJavascript("setTableMargin("+Math.round(defaultMarginWidth/this.scaleFactor)+","+Math.round(defaultMarginHeight/this.scaleFactor)+");", null);


                //this.loadUrl("javascript:setTableMargin("+Math.round(defaultMarginWidth/scaleFactor)+","+Math.round(defaultMarginHeight/scaleFactor)+");");
                //this.evaluateJavascript("setTableMargin("+Math.round(defaultMarginWidth/this.scaleFactor)+","+Math.round(defaultMarginHeight/this.scaleFactor)+");", null);

                //this.evaluateJavascript("setTableMargin("+Math.round(defaultMarginWidth/1)+","+Math.round(defaultMarginHeight/1)+");", null);
                //this.evaluateJavascript("", null);
            }
            oldScaleFactor = getScale();
            //oldScaleFactor = this.scaleFactor;
        }

        // needed for single-touch-auto-position-scroll
        scaledCrosshairPositionX = Math.round(this.getScrollX() - ((this.planWidth * scaleFactor) / 2));
        scaledCrosshairPositionY = Math.round(this.getScrollY() - ((this.planHeight * scaleFactor) / 2));

        //crosshairPositionX = Math.round(this.getScrollX() / scaleFactor - this.planWidth / 2);
        //crosshairPositionY = Math.round(this.getScrollY() / scaleFactor - this.planHeight / 2);


        //crosshairPositionX = Math.round(this.getScrollX() - ((this.planWidth * scaleFactor) / 2));
        //crosshairPositionY = Math.round(this.getScrollY() - ((this.planHeight * scaleFactor) / 2));

        //crosshairPositionX = Math.round(this.getScrollX() - ((this.planWidth * scaleFactor) / 2));
        //crosshairPositionY = Math.round(this.getScrollY() - ((this.planHeight * scaleFactor) / 2));


        if (scaleFactor > 1.1 || scaleFactor < 0.98) {
            float marginCorrectionX = (leftMargin * scaleFactor) - leftMargin;
            float marginCorrectionY = (topMargin * scaleFactor) - topMargin;


            Utils.showLogger("===========================");


            crosshairPositionX = Math.round((this.getScrollX() - marginCorrectionX) / scaleFactor - this.planWidth / 2);
            crosshairPositionY = Math.round((this.getScrollY() - marginCorrectionY) / scaleFactor - this.planHeight / 2);

/*            Utils.showLogger("scrollY>>"+this.getScrollY());
            Utils.showLogger("topMargin>>"+this.topMargin);
            Utils.showLogger("marginCorrectionY>>"+marginCorrectionY);

            Utils.showLogger("marginCorrectionY>>"+marginCorrectionY);
            Utils.showLogger("crosshairPositionY>>"+crosshairPositionY);

            Utils.showLogger("===========================");*/

            if(getScreenOrientation()== Configuration.ORIENTATION_PORTRAIT) {
                crosshairPositionY = crosshairPositionY + (getStatusBarHeight() / 2)/scaleFactor;
               // Utils.showLogger("calcu1");
            }

            if ((this.getScrollX() - marginCorrectionX) < 0.0f || (this.getScrollY() - marginCorrectionY < 0.0f) || (this.getScrollX() - marginCorrectionX > this.planWidth * scaleFactor) || (this.getScrollY() - marginCorrectionY > this.planHeight * scaleFactor)) {

                //System.out.println("##### CHECK: OUTSIDE MAP!!!");
                //this.scrollTo();
                crosshairIsOutsidePlan = true;
            } else {
                crosshairIsOutsidePlan = false;
            }

        } else {
            crosshairPositionX = Math.round((this.getScrollX()) / scaleFactor - this.planWidth / 2);
            crosshairPositionY = Math.round((this.getScrollY()) / scaleFactor - this.planHeight / 2);

            if(getScreenOrientation()== Configuration.ORIENTATION_PORTRAIT) {
                crosshairPositionY = crosshairPositionY + (getStatusBarHeight() / 2)/scaleFactor;
             //   Utils.showLogger("calcu2");
            }

        }


        // orig
//		crosshairPositionX = Math.round(this.getScrollX() / scaleFactor - this.planWidth / 2);
//		crosshairPositionY = Math.round(this.getScrollY() / scaleFactor - this.planHeight / 2);

        System.out.println("##### debugText1: oldScaleFactor:" + oldScaleFactor + " --- scaleFactor:" + this.scaleFactor + " ---" + scaleFactor + " crossHairX:" + crosshairPositionX + " crossHairY:" + crosshairPositionY);
        System.out.println("##### debugText2: oldCrossHairX:" + oldCrosshairPositionX + " oldCrossHairY:" + oldCrosshairPositionY);
        System.out.println("##### debugText3: scaledCrossHairX:" + scaledCrosshairPositionX + " oldCrossHairY:" + scaledCrosshairPositionY);
        System.out.println("##### debugText4: Math.round(this.getScrollX()):" + Math.round(this.getScrollX()) + " Math.round(this.getScrollY():" + Math.round(this.getScrollY()));
        System.out.println("##### debugText5: touchPosX" + touchPosX + " touchPosY " + touchPosY);
        System.out.println("##### debugText6: #############################################################################################################################");
    }

    public int getScreenOrientation() {
       // Utils.showLogger("screen orientation>>"+context.getResources().getConfiguration().orientation+"");
        return context.getResources().getConfiguration().orientation;
    //return 1;
    }


    public int getContentHeight() {

        int ContentHeight = 0;
        if (ContentHeight == 0)
            ContentHeight = computeVerticalScrollRange();
        return ContentHeight;
    }

    public int getContentWidth() {
        int ContentHeight = 0;
        if (ContentHeight == 0)
            ContentHeight = computeHorizontalScrollRange();
        return ContentHeight;
    }


}
