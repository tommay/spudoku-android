main = do
  interact $ map color

color :: Char -> Char
color c =
  case c of
    'R' -> '1'
    'O' -> '2'
    'Y' -> '3'
    'g' -> '4'
    'G' -> '5'
    'B' -> '6'
    'b' -> '7'
    'L' -> '8'
    'P' -> '9'
    _ -> c
