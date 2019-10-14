-- 10-50ms delay before each request
function delay()
   return math.random(10, 50)
end

-- Load URL paths from the file
function load_url_paths_from_file(file)
  lines = {}

  -- Check if the file exists
  -- Resource: http://stackoverflow.com/a/4991602/325852
  local f=io.open(file,"r")
  if f~=nil then
    io.close(f)
  else
    -- Return the empty array
    return lines
  end

  -- If the file exists loop through all its lines
  -- and add them into the lines array
  for line in io.lines(file) do
    if not (line == '') then
      lines[#lines + 1] = line
    end
  end

  return lines
end

-- Load URL paths from file
paths = load_url_paths_from_file("/data/requests.txt")
print("multiplepaths: Found " .. #paths .. " paths")

-- Initialize the paths array iterator
counter = 0

request = function()
  delay()
  -- Get the next paths array element
  url_path = paths[counter]
  counter = counter + 1
  -- If the counter is longer than the paths array length then reset it
  if counter > #paths then
    counter = 0
  end
  -- Return the request object with the current URL path
  return wrk.format(nil, url_path)
end

-- Generates latency report
done = function(summary, latency, requests)
   io.write("------------------------------\n")
   for _, p in pairs({ 50, 90, 99, 99.999 }) do
      n = latency:percentile(p)
      io.write(string.format("%g%%,%d\n", p, n))
   end
end
