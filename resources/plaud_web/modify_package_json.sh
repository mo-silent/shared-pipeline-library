#!/bin/bash
# 从package.json中提取name和pipeline字段并组合成subpath
name=$(jq -r '.name' package.json)
pipeline=$(jq -r '.pipeline // ""' package.json)
subpath="${name}${pipeline}"

# 移除subpath中的所有点号
subpath=$(echo "$subpath" | tr -d '.')

# 更新package.json，添加subpath字段
tmp=$(mktemp)
jq --arg subpath "$subpath" '.name = $subpath | if has("publicPath") then .publicPath = $subpath else . end' package.json > "$tmp" && mv "$tmp" package.json