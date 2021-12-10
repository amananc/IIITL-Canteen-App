package com.anchal.canteentest.Common;

import com.anchal.canteentest.Model.User;


public class Common
{
    public static User currentUser;
    private static final String GOOGLE_API_URL = "https://maps.googleapis.com/";

    public static com.anchal.canteentest.Remote.IGoogleService getGoogleMapsAPI() {
        return com.anchal.canteentest.Remote.RetrofitClient.getGoogleClient(GOOGLE_API_URL).create(com.anchal.canteentest.Remote.IGoogleService.class);
    }

    public static String convertCodeToStatus(String status) {
        if(status.equals("0"))
            return "Order Placed";

        else if(status.equals("1"))
            return "Order on the Way!";

        else
            return "Order Delivered";
    }

    public static int returnStatusCode(String status)
    {
        if(status.equals("0"))
            return 0;

        else if(status.equals("1"))
            return 1;

        else
            return 2;
    }
}