function tree-exclude {
    param(
        [string]$Path = ".",
        [string[]]$Exclude = @()
    )

    function Show-Tree {
        param(
            [string]$CurrentPath,
            [string]$Prefix = ""
        )

        $items = @(Get-ChildItem -LiteralPath $CurrentPath |
            Where-Object { $_.Name -notin $Exclude })

        for ($i = 0; $i -lt $items.Count; $i++) {
            $item = $items[$i]
            $isLast = $i -eq ($items.Count - 1)

            if ($isLast) {
                $branch = "\-- "
                $nextPrefix = "$Prefix    "
            }
            else {
                $branch = "|-- "
                $nextPrefix = "$Prefix|   "
            }

            Write-Output "$Prefix$branch$($item.Name)"

            if ($item.PSIsContainer) {
                Show-Tree -CurrentPath $item.FullName -Prefix $nextPrefix
            }
        }
    }

    Write-Output (Split-Path (Resolve-Path $Path) -Leaf)
    Show-Tree -CurrentPath (Resolve-Path $Path)
}