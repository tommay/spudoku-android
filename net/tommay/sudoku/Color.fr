main = do
  interact $ map color

color :: Char -> Char
color c =
  case c of
    '1' -> 'R'
    '2' -> 'O'
    '3' -> 'Y'
    '4' -> 'g'
    '5' -> 'G'
    '6' -> 'B'
    '7' -> 'b'
    '8' -> 'L'
    '9' -> 'P'
    _ -> c
