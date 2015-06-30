<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="/thor-toolkit" prefix="tt" %>
<!DOCTYPE html>
<html>
<head>
	<tt:env/><tt:loc bundle="message"/>
    <title>${loc.get("title")}</title>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <link rel="stylesheet" href="${root}/assets/script/tui/css/tui.min.css">
    <link href="${root}/assets/script/font-awesome-4.2.0/css/font-awesome.min.css" rel="stylesheet">

    <script src="${root}/assets/script/moment.js"></script>
    <script src="${root}/assets/script/jquery-1.11.1.min.js"></script>
    <script src="${root}/assets/script/tui/tui.all.js"></script>
    <script src="${root}/assets/script/tui/lang/zh-cn.js"></script>
    <script src="${root}/assets/script/tui/lang/en-us.js"></script>

</head>
<body>
    <h1>map</h1>
    <div id="map-data"></div>
    <div style="height:310px;width:681px;" id="map"></div>

    <script src="${root}/assets/script/echarts/echarts.js"></script>
    <script type="text/javascript">
      require.config({
        paths: {
          echarts: "${root}/assets/script/echarts"
        }
      });
    </script>
    <script type="text/javascript">
      $(document).ready(function() {
        $("div#map-data").html("...");
        $.get("http://localhost:8080/getVisitDistribution.do?dimension=province",
          function(result) {
            $("div#map-data").html(JSON.stringify(result));
            var mapdata = [];
            var maxdata = 0;
            for (var key in result) {
              var entry = {};
              entry["name"] = key;
              entry["value"] = result[key];
              if (maxdata < entry["value"])
                maxdata = entry["value"];
              mapdata.push(entry);
            }
            require(
              ['echarts', 'echarts/chart/map'],
              function (ec) {
                var mapChart = ec.init(document.getElementById('map'));
                var option = {
                  title: {
                    text: '访问来源地理分布',
                    x: 'center'
                  },
                  tooltip: {
                    trigger: 'item'
                  },
                  dataRange: {
                    min: 0,
                    max: Math.ceil(maxdata),
                    x: 'left',
                    y: 'bottom',
                   // color: ['orangered', 'yellow', 'lightskyblue'],
                   color: ['orangered', 'lightskyblue'],
                    text: ['high', 'low'],
                    calculable: true,
                    show: true
                  },
                  series: [
                    {
                      name: 'visit',
                      type: 'map',
                      mapType: 'china',
                      roam: true,
                      itemStyle: {
                       // normal: { label: { show: true } },
                        emphasis: { label: { show: true } }
                      },
                      data: mapdata
                    }
                  ]
                };
                mapChart.setOption(option);
              }
            );
          }).fail(function (jqXHR, textStatus, errorThrown) {
            alert(JSON.stringify(jqXHR));
          });
      });
    </script>
</body>
</html>

