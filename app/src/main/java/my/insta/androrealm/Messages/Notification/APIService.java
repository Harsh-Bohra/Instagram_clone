package my.insta.androrealm.Messages.Notification;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=BDWFqQfrQaEwe0oaQM7JLEULlBDuEv9UcLwOh8c746xdQvAx2zgKUBB7tALQgP4TOBnzbrYFwaO5M11wO9gb54M"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body NotificationSender body);


}
