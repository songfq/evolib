#!/bin/bash
# backup-db.sh — PostgreSQL 每日备份脚本
# 用法：bash backup-db.sh
# crontab 自动执行：0 3 * * * /path/to/backup-db.sh >> /var/log/evolib-backup.log 2>&1

BACKUP_DIR="/backup/evolib"
DB_NAME="evolib"
DB_USER="evolib"
RETENTION_DAYS=7

mkdir -p "$BACKUP_DIR"

echo "[$(date '+%Y-%m-%d %H:%M:%S')] 开始备份数据库..."

# pg_dump 导出（-F c 为自定义压缩格式，体积小）
pg_dump -U "$DB_USER" -d "$DB_NAME" -F c \
  -f "$BACKUP_DIR/evolib_$(date +%Y%m%d_%H%M%S).dump"

if [ $? -eq 0 ]; then
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] 备份完成: evolib_$(date +%Y%m%d_%H%M%S).dump"

    # 清理超过保留天数的旧备份
    DELETED=$(find "$BACKUP_DIR" -name "*.dump" -mtime +"$RETENTION_DAYS" -print -delete | wc -l)
    if [ "$DELETED" -gt 0 ]; then
        echo "[$(date '+%Y-%m-%d %H:%M:%S')] 清理旧备份: $DELETED 个文件"
    fi
else
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] ❌ 备份失败！"
    exit 1
fi

# 恢复命令（供参考）：pg_restore -U evolib -d evolib /backup/evolib/evolib_20260710.dump
