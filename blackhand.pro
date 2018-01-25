#-extends android.support.v7.app.AppCompatActivity{
#    public Z onActivityTest (Ljava/lang/String;) false;
#    private Ljava/lang/String; onActivity2 (Ljava/lang/String;) null;
#    public V onTESTv ();
#    private_final I ontesti () 1;
#}
#
#-replaceExtends whichExtends com.meetyou.blackhand.demo.MainActivity{
#    android.support.v7.app.AppCompatActivity;
#}
-extends android.support.v7.app.AppCompatActivity{
    public Landroid/content/res/Resources; getResources () null;
    public Landroid/content/res/AssetManager; getAssets () null;
    public Landroid/content/res/Resources/Theme; getTheme () null;
}
-extends android.app.Activity{
    public Landroid/content/res/Resources; getResources () null;
    public Landroid/content/res/AssetManager; getAssets () null;
    public Landroid/content/res/Resources/Theme; getTheme () null;
}
-extends android.support.v4.app.FragmentActivity{
    public Landroid/content/res/Resources; getResources () null;
    public Landroid/content/res/AssetManager; getAssets () null;
    public Landroid/content/res/Resources/Theme; getTheme () null;
}
#-delete method {
#    public V onReceivedSslError (Landroid/webkit/WebView;Landroid/webkit/SslErrorHandler;Landroid/net/http/SslError;);
#}
#-exchangeXML layout{
#    com.meetyou.blackhand.demo com.linhh.blackhand.demo;
#}
-printInfo method {
    public V onReceivedSslError (Landroid/webkit/WebView;Landroid/webkit/SslErrorHandler;Landroid/net/http/SslError;);
    public V test222 (II);
}