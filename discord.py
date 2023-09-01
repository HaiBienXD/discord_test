import discord
from discord.ext import commands, tasks
from discord_components import DiscordComponents, Button, ButtonStyle

intents = discord.Intents.default()
intents.members = True

bot = commands.Bot(command_prefix='!', intents=intents)
DiscordComponents(bot)

@bot.event
async def on_ready():
    print(f'Logged in as {bot.user.name}')
    check_inactive_members.start()

@tasks.loop(hours=24)  # Lặp lại mỗi 24 giờ
async def check_inactive_members():
    guild_id = 864525371453079572  # Thay bằng ID của server bạn muốn loại bỏ thành viên
    days_inactive = 30  # Số ngày không hoạt động để xem là không hoạt động
    last_check_date = datetime.datetime.now()

    guild = bot.get_guild(guild_id)

    if not guild:
        print('Server không tồn tại.')
        return

    inactive_members = [member for member in guild.members if not member.activity or (last_check_date - member.activity.created_at).days > days_inactive]

    # Tạo Embed Message
    embed = discord.Embed(title="Loại bỏ Thành viên Không Hoạt Động", description=f"Có {len(inactive_members)} thành viên không hoạt động. Bạn có muốn tiếp tục?", color=0xff0000)

    # Thêm nút xác nhận
    confirm_button = Button(style=ButtonStyle.green, label="Xác Nhận", custom_id="confirm")
    embed.set_footer(text="Đợi phản hồi của bạn...")
    
    # Gửi thông báo vào kênh chat
    channel_id = 1062804079379742780  # Thay bằng ID của kênh bạn muốn gửi thông báo
    channel = bot.get_channel(channel_id)
    message = await channel.send(embed=embed, components=[[confirm_button]])

    # Chờ phản hồi từ người dùng
    def check_interaction(interaction):
        return interaction.message.id == message.id and interaction.user.id != bot.user.id

    try:
        interaction = await bot.wait_for("button_click", timeout=60.0, check=check_interaction)
        if interaction.custom_id == "confirm":
            for member in inactive_members:
                await member.kick()
    except asyncio.TimeoutError:
        await message.edit(embed=discord.Embed(description="Hết thời gian cho phản hồi. Không loại bỏ thành viên.", color=0xff0000), components=[])

@bot.command()
async def start_inactive_check(ctx):
    check_inactive_members.start()
    await ctx.send('Bắt đầu kiểm tra thành viên không tương tác hàng ngày.')

@bot.command()
async def stop_inactive_check(ctx):
    check_inactive_members.stop()
    await ctx.send('Dừng kiểm tra thành viên không tương tác hàng ngày.')

# Thay YOUR_BOT_TOKEN bằng mã token của bạn
bot.run('MTE0NzE1MDY0MDMwNTQxNDE2NA.GkK6K9.-f6iNo_NlbVYv4cYRig-k3xNTR9ynEwwaXhIRA')
