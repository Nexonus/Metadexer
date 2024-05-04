cd ".\samples\artificial"
$file = "Perlin2.jpg"

$(Get-Item $file).lastaccesstime=$(Get-Date "12/02/1990")
$(Get-Item $file).lastwritetime=$(Get-Date "12/02/1990")
$(Get-Item $file).creationtime=$(Get-Date "12/02/1990")