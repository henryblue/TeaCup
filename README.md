# TeaCup
A Material Design app for entertainment

##functional list

- fine interface
- read fresh news
- read excellent article
- hear the unique music
- listen to the radio
- view picture

##ScreenShot
<p><img src="screenshot/photo1.jpg" width="32%" />
<img src="screenshot/photo2.jpg" width="32%" />
<img src="screenshot/photo3.jpg" width="32%" />
<img src="screenshot/photo4.jpg" width="32%" />
<img src="screenshot/photo5.jpg" width="32%" />
<img src="screenshot/photo6.jpg" width="32%" />
<img src="screenshot/photo7.jpg" width="32%" />
<img src="screenshot/photo8.jpg" width="32%" />
<img src="screenshot/photo9.jpg" width="32%" /></p>

## Implementation

### Data

Most data are fetched from network while some of them are cached for offline.

- okhttp with custom extensions for network requests.
- Gson for data model.
- Glide for image loading.
- jsoup for parse html.

Data sources:

- [煎蛋网](http://jandan.net/)
- [豆瓣读书](https://book.douban.com/)
- [神马电影网](http://www.82ke.com/)
- [落网](http://www.luoo.net/)
- [糗事百科](http://www.qiushibaike.com/)
- [第一弹](http://www.diyidan.com/)

### UI

- Material Design implemented with AppCompat, Design, CardView and RecyclerView from support library and some customization.
- Animation implemented with shared element transition on Lollipop and above.

### location
Use Baidu maps to locate.

## Libraries created for this project

- [MiniMusicView](https://github.com/henryblue/MiniMusicView)
- [MxVideoPlayer](https://github.com/henryblue/MxVideoPlayer)
- [okhttp](https://github.com/square/okhttp)
- [xrecyclerview](https://github.com/jianghejie/XRecyclerView)
- [circleimageview](https://github.com/hdodenhof/CircleImageView)
- [materialdialog](https://github.com/drakeet/MaterialDialog)

## Third party libraries

- [PhotoView](https://github.com/chrisbanes/PhotoView)
- [Glide](https://github.com/bumptech/glide)
- [Gson](https://github.com/google/gson)
- [jsoup](https://jsoup.org/download)

## Building

You can download the APK file from [releases](/teacup-1.2_3.apk) of this project.


##License

```
Copyright 2016 henryblue

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
