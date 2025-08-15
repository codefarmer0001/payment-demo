#!/bin/bash

scp -i /workspace/payment/login_aws_ec2.pem ./target/payment-*.jar ubuntu@$host:~/

ssh -o ServerAliveInterval=20 -i /workspace/payment/login_aws_ec2.pem ubuntu@$host << EOF
# 切换到root账户下面
sudo -i
# 进入到 /opt/admin 文件夹下面
cd /opt/demo
# 先停止已经运行的进程
sh stop
# 将文件备份到被分文件夹
mv payment*.jar back/
# 将jar文件移动到 /opt/admin 文件夹下面
mv /home/ubuntu/payment-*.jar ./
# 然后重启服务
# 启动服务（不通过管道）
sh start
echo "✅ 远程部署 $host 的 admin 完成！"
EOF