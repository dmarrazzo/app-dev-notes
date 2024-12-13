# Examples

## Count Favorite colors

input user and color:

stephane,blue
john,green
stephane,red
alice,red

Output:

green,1
blue,0
red,2

Code:

```java
KTable<String, String> colorInput = builder.table("color-input");

colorInput
        .mapValues(v -> v.toLowerCase())
        .filter((k, v) -> v.matches("blue|green|red"));

var colorCount = colorInput.groupBy((key, value) -> KeyValue.pair(value, key))
        .count();

colorCount.toStream().to("color-count", Produced.with(Serdes.String(), Serdes.Long()));
```
