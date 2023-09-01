import javax.security.auth.login.LoginException;
import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.*;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.RestAction;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MyBot {
    public static void main(String[] args) {
        try {
            JDABuilder builder = JDABuilder.createDefault("YOUR_BOT_TOKEN");
            builder.enableIntents(GatewayIntent.GUILD_MEMBERS);
            builder.addEventListeners(new MyListener());
            JDA jda = builder.build();
            jda.awaitReady();
        } catch (LoginException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class MyListener extends ListenerAdapter {
    private static final int daysInactive = 30;
    private static final long checkInterval = 24 * 60 * 60 * 1000; // 24 hours in milliseconds
    private static final String targetChannelId = "1062804079379742780"; // ID của kênh chat cần nhắn

    @Override
    public void onReady(ReadyEvent event) {
        System.out.println("Bot đã sẵn sàng!");
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkInactiveMembers(event.getJDA());
            }
        }, 0, checkInterval);
    }

    private void checkInactiveMembers(JDA jda) {
        OffsetDateTime lastCheckDate = OffsetDateTime.now();
        Guild guild = jda.getGuildById("YOUR_GUILD_ID"); // Thay YOUR_GUILD_ID bằng ID của máy chủ bạn muốn kiểm tra
        if (guild == null) {
            System.out.println("Máy chủ không tồn tại.");
            return;
        }

        List<Member> members = guild.loadMembers().get();
        for (Member member : members) {
            if (member.getActivities().isEmpty() || lastCheckDate.minusDays(daysInactive).isAfter(member.getTimeJoined())) {
                // Làm điều gì đó với thành viên không hoạt động ở đây, ví dụ: loại bỏ, gửi thông báo, vv.
                System.out.println("Loại bỏ thành viên: " + member.getEffectiveName());

                // Gửi thông báo và nút xác nhận vào kênh chat
                sendConfirmationMessage(member, guild.getTextChannelById(targetChannelId));
            }
        }
    }

    private void sendConfirmationMessage(Member member, TextChannel channel) {
        if (channel != null) {
            int membersToRemove = 1; // Số thành viên sẽ bị xoá
            String message = String.format("Chúng tôi đã phát hiện %d thành viên không hoạt động. Bạn có muốn tiếp tục?", membersToRemove);

            Button confirmButton = Button.primary("confirm", "Xác Nhận");
            ActionRow actionRow = ActionRow.of(confirmButton);

            channel.sendMessage(message).setActionRow(actionRow).queue(interactionHook -> {
                // Thêm logic xử lý sau khi người dùng nhấn nút xác nhận
                interactionHook.retrieveOriginal().queue(originalMessage -> {
                    originalMessage
                            .editMessage("Bạn đã xác nhận. Thực hiện loại bỏ " + membersToRemove + " thành viên.")
                            .setActionRow().queue();
                });
            });
        }
    }
}
