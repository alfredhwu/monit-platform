package com.accenture.lab;

import com.accenture.lab.services.HttpClientService;
import com.accenture.lab.services.IPGeoService;
import com.accenture.lab.services.VisitDistributionService;
import com.github.thorqin.toolkit.db.DBService;
import com.github.thorqin.toolkit.web.WebApplication;
import com.github.thorqin.toolkit.web.annotation.WebApp;
import com.github.thorqin.toolkit.web.annotation.WebAppService;
import com.github.thorqin.toolkit.web.annotation.WebRouter;

@WebApp(name = "monit-platform",
        routers = {
                @WebRouter("*.do")
                // Uncomment following line to enable database router
                // , @WebRouter(value = "/db/*", type = MyApplication.MyDBRouter.class)
        },
        service = {
                @WebAppService(name = "db", type = DBService.class),
                @WebAppService(name = "http-client", type = HttpClientService.class),
                @WebAppService(name = "ip-geo", type = IPGeoService.class),
                @WebAppService(name = "visit-distribution", type = VisitDistributionService.class)
        }
)
public class MyApplication extends WebApplication {
    /* Uncomment following lines to enable database router
    @DBRouter
    public static class MyDBRouter extends WebDBRouter {
        public MyDBRouter(WebApplication application) throws ValidateException {
            super(application);
        }
    }
    */
}


