# Blackhand

Blackhand可以帮助你在编译期往需要的class中新增方法。

## 什么是Blackhand

它能帮助你往任意class中新增方法,非侵入,只需要配置文件即可,可操作第三方jar、support包、主工程以及模块等。

结合Anna库可以达到更好的效果。

## 可以做什么

比如你想在每个Activity内实现父类的onDestory方法,因为有的Activity可能没有复写onDestory,所以通过Blackhand可以在编译期新增它,再通过Anna来为onDestory实现其中功能。

## 使用方法

### 添加插件

在主工程的build.gradle中加入

```groovy
classpath 'com.meiyou.sdk.plugin:blackhand-compiler:0.0.10-SNAPSHOT'
```

在你的主工程中加入插件,如果你要结合anna,需要将其置于Anna前

```groovy
apply plugin: 'blackhand'

```

## 用法

### 新增方法作用域

在主工程根目录创建文件"blackhand.pro",书写内容如下:

```groovy
-extends android.support.v7.app.AppCompatActivity{
    public Z onActivityTest (Ljava/lang/String;) false;
    private Ljava/lang/String; onActivity2 (Ljava/lang/String;) null;
    public V onTESTv ();
    private_final I ontesti () 1;
}
```

"-"后的表示应用修改作用域, 上述的extends android.support.v7.app.AppCompatActivity表示的是所有继承AppCompatActivity的类被应用修改。

```groovy
-implements com.meetyou.ITest{

}
```

所以类似于这种,表示实现了ITest接口的类被修改。

```groovy
-class com.meetyou.A{

}
```

上述表示修改的是类com.meetyou.A的内容。

### 新增方法描述

```groovy
-extends android.support.v7.app.AppCompatActivity{
    public Z onActivityTest (Ljava/lang/String;) false;
    private Ljava/lang/String; onActivity2 (Ljava/lang/String;) null;
    public V onTESTv ();
    private_final I ontesti () 1;
}
```

仍然以这个作为例子,我们知道这个例子的作用域是继承AppCompatActivity的类。

{}内的内容表示需要新增的方法。

写法上与原生Java代码相似,第一个参数表示修饰符,第二个表示方法的返回值类型,第三个参数表示方法名,第四个()内表示入参,最后的表示默认返回值。

可以看到类型都是以JNI字段描述符来表示的。

上述例子用java代码表示如下:

```groovy
    public boolean onActivityTest (String arg){
     return false;
    }

    private String onActivity2 (String arg){
        return null;
    }

    public void onTESTv (){

    }

    private final int ontesti (){
      return 1;
    }
```

### 修改继承

Blackhand可以修改类的继承,将原本的继承改为其他类。同样需要在pro配置作用域。

```groovy
-replaceExtends whichExtends com.meetyou.blackhand.demo.MainActivity{
    android.support.v7.app.AppCompatActivity;
}
```


上述配置意味,将继承com.meetyou.blackhand.demo.MainActivity的类改为继承android.support.v7.app.AppCompatActivity。


```groovy
-replaceExtends whichImplements com.meetyou.blackhand.demo.MainActivity{
    android.support.v7.app.AppCompatActivity;
}
```

这个表示实现了这个接口的将被修改。

## 完成编译

配置和操作完毕后,重新编译或者运行就可以发现你的新增方法生效了

## 配合ANNA

Anna地址:(https://github.com/HomHomLin/Anna)

当你新增方法后,你可以通过Anna来监听该方法的执行,并且通过Anna重写该新增方法。

在必要的地方监听Anna,再调用新增方法实现新增方法的调度。

### 示例

如你需要再Class A中新增方法Method A,并且在onCreate中调用Method A。

你可以通过Blackhand为Class A新增方法Method A,然后在Anna中监听Method A,并且拦截Method A,实现功能。

再通过Anna监听onCreate,增加对当前对象的Method A的反射使用。

## Developed By

 * Linhonghong - <QQ:371655539，mail:371655539@qq.com>

## Attention
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
