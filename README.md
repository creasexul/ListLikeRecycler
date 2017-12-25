# ListLikeRecycler
Custom RecyclerView writed with [Kotlin](https://github.com/JetBrains/kotlin)

# Usage

## init
Just init it like a simple RecyclerView:
``` Kotlin
val layoutManager = LinearLayoutManager(this)
layoutManager.orientation = LinearLayoutManager.VERTICAL
recyclerView.layoutManager = layoutManager
recyclerView.adapter = adapter
```

## enable head refreshing and foot loading
These features are disabled by default. You can enable these features in XML or in your code:

``` xml
<com.crease.listlikerecyclerview.view.ListLikeRecyclerView
    android:id="@+id/recyclerView"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    app:layoutManager="LinearLayoutManager"
    app:loadable="true"
    app:refreshable="true"/>
```

``` Kotlin
recyclerView.refreshEnabled = true
recyclerView.loadingEnabled = true
```

## set refreshing status
``` Kotlin
recyclerView.isRefreshing = true
```

## set RefreshListener
``` Kotlin
recyclerView.loadCallback = object : ListLikeRecyclerView
            .OnRecyclerViewLoadCallback {
            override fun onHeadRefresh() {
                mainBinding.root.postDelayed({
                    Toast.makeText(this@MainActivity, "Head refreshing finished", Toast
                            .LENGTH_LONG)
                            .show()
                    mainBinding.recyclerView.isRefreshing = false
                }, 1000)
            }

            override fun onFootLoad() {
                mainBinding.root.postDelayed({
                    Toast.makeText(this@MainActivity, "Foot loading finished", Toast.LENGTH_LONG).show()
                    // no more
                    mainBinding.recyclerView.noMore = true
                }, 1000)
            }
        }
```

## set ItemClickListener
``` Kotlin
recyclerView.setOnItemClickListener(object : ListLikeRecyclerView
            .OnItemClickListener {
            override fun onItemClick(childView: View, itemPosition: Int) {
                Toast.makeText(this@MainActivity, itemPosition.toString(), Toast.LENGTH_LONG).show()
            }

            override fun onItemLongClick(childView: View, position: Int) {

            }
        })
```

## custom header and footer
If you need customized header/footer, you can inherit `HeadRefreshView` or `FootLoadView` and implement the corresponding method

## add/remove header/footer
``` Kotlin
recyclerView.addHeaderView(id, view)
recyclerView.addFooterView(id, view)
recyclerView.removeHeaderView(id)
recyclerView.removeFooterView(id)
```

# License

>Copyright 2017 Crease
>
>Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
>
>   http://www.apache.org/licenses/LICENSE-2.0
>
>Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
