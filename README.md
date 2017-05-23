# `AndroidTagGroup`


# Install

```groovy
repositories{
  maven {url 'http://maven.mjtown.cn/'}
}

dependencies{
  compile 'name.zeno:tag-group:1.0.0'
}
```

# Attributes

There are several attributes you can set:

![Dimension illustrate](http://ww2.sinaimg.cn/large/bce2dea9gw1epov0i8x6kj20rk054q4g.jpg)

|           attr        	|     default      |                         mean                          	 |
|:------------------------- |:---------------- |:------------------------------------------------------- |
| atg_isAppendMode      	| false            | Determine the TagGroup mode, APPEND or single DISPLAY.  |
| atg_inputHint   	        | Add Tag/添加标签  | The hint of the INPUT tag.                              |
| atg_borderColor	        | #49C120          | The tag outline border color.                           |
| atg_textColor          	| #49C120          | The tag text color.                           	         |
| atg_backgroundColor       | #FFFFFF          | The tag background color.                               |
| atg_dashBorderColor       | #AAAAAA          | The tag dash outline border color.                      |
| atg_inputHintColor        | #80000000        | The input tag hint text color.                          |
| atg_inputTextColor        | #DE000000        | The input tag type text color..                         |
| atg_checkedBorderColor    | #49C120          | The checked tag outline border color.                   |
| atg_checkedTextColor      | #FFFFFF          | The checked text color.                                 |
| atg_checkedMarkerColor    | #FFFFFF          | The checked marker color.                               |
| atg_checkedBackgroundColor| #49C120          | The checked tag background color.                       |
| atg_pressedBackgroundColor| #EDEDED          | The tag background color when the tag is being pressed. |
| atg_borderStrokeWidth     | 0.5dp            | The tag outline border stroke width.        	         |
| atg_borderRadius  	    | -1               | 边框圆角大小                                              |
| atg_textSize          	| 13sp             | The tag text size.                                  	 |
| atg_horizontalSpacing     | 8dp              | The horizontal tag spacing.(Mark1)                      |
| atg_verticalSpacing  	    | 4dp              | The vertical tag spacing.(Mark2)                      	 |
| atg_horizontalPadding	    | 12dp             | The horizontal tag padding.(Mark3)                      |
| atg_verticalPadding  	    | 3dp              | The vertical tag padding.(Mark4)                        |
