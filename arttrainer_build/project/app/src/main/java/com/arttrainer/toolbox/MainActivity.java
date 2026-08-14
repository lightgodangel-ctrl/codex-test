package com.arttrainer.toolbox;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private TextView status;
    private SharedPreferences ux;
    @Override protected void onCreate(Bundle b) { super.onCreate(b); ux=OverlayPrefs.prefs(this); buildUi(); maybeRequestNotifications(); }
    @Override protected void onResume() { super.onResume(); refreshStatus(); }
    private void buildUi() {
        ScrollView scroll=new ScrollView(this); LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(dp(20),dp(24),dp(20),dp(32)); root.setGravity(Gravity.CENTER_HORIZONTAL); scroll.addView(root,new ScrollView.LayoutParams(-1,-2));
        TextView title=new TextView(this); title.setText("ArtTrainer All-in-One v3"); title.setTextSize(26f); title.setTextColor(Color.BLACK); title.setGravity(Gravity.CENTER); root.addView(title,matchWrap(dp(10)));
        TextView sub=new TextView(this); sub.setText("그림 연습용 플로팅 진단 도구\n탭=퀵팔레트 · 더블탭=원래색 · 드래그=이동"); sub.setTextSize(15f); sub.setTextColor(0xff444444); sub.setGravity(Gravity.CENTER); root.addView(sub,matchWrap(dp(16)));
        status=new TextView(this); status.setTextSize(14f); status.setTextColor(0xff222222); status.setPadding(dp(14),dp(12),dp(14),dp(12)); status.setBackgroundColor(0xffeeeeee); root.addView(status,matchWrap(dp(14)));
        addButton(root,"1) 다른 앱 위에 표시 허용",v->openOverlayPermission());
        addButton(root,"2) MacroDroid 브리지 테스트",v->{ MacroDroidBridge.toggle(this); Toast.makeText(this,"MacroDroid에 흑백 토글 신호 전송",Toast.LENGTH_SHORT).show(); });
        addButton(root,"3) 플로팅 도구 시작",v->startOverlay()); addButton(root,"플로팅 도구 중지 + 원래색 복구",v->stopOverlayAndRestore());
        addSection(root,"플로팅 UX"); addCheck(root,"화면 가장자리 자동 붙기",OverlayPrefs.K_EDGE_SNAP,true); addCheck(root,"안 쓸 때 자동으로 흐리게",OverlayPrefs.K_AUTO_DIM,true); addCheck(root,"부팅 후 자동 시작",OverlayPrefs.K_BOOT_START,false);
        addSeek(root,"버튼 크기",OverlayPrefs.K_SIZE_DP,40,82,54,"dp"); addSeek(root,"버튼 투명도",OverlayPrefs.K_OPACITY,35,100,92,"%"); addSeek(root,"대기 시 투명도",OverlayPrefs.K_DIM_OPACITY,10,65,28,"%"); addSeek(root,"자동 흐림 대기",OverlayPrefs.K_DIM_DELAY,2,20,6,"초");
        addButton(root,"플로팅 위치 초기화",v->{ getSharedPreferences("bubble",MODE_PRIVATE).edit().clear().apply(); notifyOverlaySettingsChanged(true); Toast.makeText(this,"위치 초기화",Toast.LENGTH_SHORT).show(); });
        addSection(root,"퀵팔레트"); TextView q=new TextView(this); q.setText("탭하면 ↺ 원래색 · 흑 흑백 LIVE · 3 3값 · ≈ 블러 · ⇆ 좌우반전 · ◐ 색반전\n3값/블러/좌우반전은 화면 캡처 동의 후 분석 프리뷰로 열린다. 더블탭은 즉시 원래색 복구."); q.setTextSize(14f); q.setTextColor(0xff333333); q.setPadding(dp(4),dp(4),dp(4),dp(8)); root.addView(q,matchWrap(dp(8)));
        addSection(root,"전체 분석"); TextView n=new TextView(this); n.setText("분석 캡처: 흑백 · 저채도 · 좌우반전 · 블러 · 2값 · 3값 · 축소 · RGB 채널\n빠른 설정의 흑백 타일도 MacroDroid 브리지로 작동."); n.setTextSize(14f); n.setTextColor(0xff333333); root.addView(n,matchWrap(0)); setContentView(scroll);
    }
    private void addSection(LinearLayout r,String t){ TextView v=new TextView(this); v.setText(t); v.setTextSize(18f); v.setTextColor(0xff111111); v.setPadding(0,dp(18),0,dp(8)); r.addView(v,matchWrap(0)); }
    private void addCheck(LinearLayout r,String l,String k,boolean d){ CheckBox c=new CheckBox(this); c.setText(l); c.setTextSize(15f); c.setChecked(ux.getBoolean(k,d)); c.setOnCheckedChangeListener((b,x)->{ux.edit().putBoolean(k,x).apply();notifyOverlaySettingsChanged(false);}); r.addView(c,matchWrap(dp(4))); }
    private void addSeek(LinearLayout r,String l,String k,int min,int max,int def,String s){ TextView v=new TextView(this); int cur=Math.max(min,Math.min(max,ux.getInt(k,def))); v.setText(l+" : "+cur+s); r.addView(v,matchWrap(0)); SeekBar b=new SeekBar(this); b.setMax(max-min); b.setProgress(cur-min); b.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){ public void onProgressChanged(SeekBar x,int p,boolean f){int z=min+p;v.setText(l+" : "+z+s);if(f){ux.edit().putInt(k,z).apply();notifyOverlaySettingsChanged(false);}} public void onStartTrackingTouch(SeekBar x){} public void onStopTrackingTouch(SeekBar x){} }); r.addView(b,matchWrap(dp(8))); }
    private void addButton(LinearLayout r,String t,View.OnClickListener l){ Button b=new Button(this); b.setAllCaps(false); b.setText(t); b.setOnClickListener(l); r.addView(b,matchWrap(dp(8))); }
    private LinearLayout.LayoutParams matchWrap(int m){ LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2); p.bottomMargin=m; return p; }
    private void refreshStatus(){ if(status==null)return; boolean o=Settings.canDrawOverlays(this),m=false; try{getPackageManager().getPackageInfo("com.arlosoft.macrodroid",0);m=true;}catch(Throwable ignored){} status.setText("다른 앱 위 표시: "+(o?"허용됨":"필요")+"\nMacroDroid: "+(m?"설치됨 · 브리지 매크로 필요":"설치 필요")+"\nArtTrainer용 ADB 추가 권한: 필요 없음\n캡처 세션: "+(CaptureService.isRunning()?"실행 중":"꺼짐")); }
    private void openOverlayPermission(){ startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:"+getPackageName()))); }
    private void startOverlay(){ if(!Settings.canDrawOverlays(this)){Toast.makeText(this,"먼저 다른 앱 위에 표시를 허용해줘",Toast.LENGTH_LONG).show();openOverlayPermission();return;} Intent i=new Intent(this,OverlayService.class);i.setAction(OverlayService.ACTION_START);if(Build.VERSION.SDK_INT>=26)startForegroundService(i);else startService(i);Toast.makeText(this,"플로팅 도구 시작",Toast.LENGTH_SHORT).show();refreshStatus(); }
    private void notifyOverlaySettingsChanged(boolean reset){ if(!OverlayService.isRunning())return; try{Intent i=new Intent(this,OverlayService.class);i.setAction(OverlayService.ACTION_REFRESH_SETTINGS);i.putExtra("resetPosition",reset);startService(i);}catch(Throwable ignored){} }
    private void stopOverlayAndRestore(){ Intent c=new Intent(this,CaptureService.class);c.setAction(CaptureService.ACTION_STOP);startService(c); Intent o=new Intent(this,OverlayService.class);o.setAction(OverlayService.ACTION_STOP);startService(o); MacroDroidBridge.restore(this); }
    private void maybeRequestNotifications(){ if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},1001); }
    private int dp(int v){ return Math.round(v*getResources().getDisplayMetrics().density); }
}
