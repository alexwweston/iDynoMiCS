function makeMovieALL(nFrames,sName, c1, c2)
%
%   Traverse a directory, calling make_AVI_movie(nFrames,sName, c1, c2, fileName) o
%   on all result folders.
%   Takes same arguments as make_AVI_movie, except filename--this will be
%   auto-generated based on each result folder's name
%

allFiles=dir(pwd);

for i=1:length(allFiles)
    if length(allFiles(i).name)>10
        folder = char(allFiles(i).name)
        cd(folder)
        name = pwd;
        [upperPath, deepestFolder, ~] = fileparts(name);
        make_AVI_movie(nFrames,sName, c1, c2, strcat('../../movies/',deepestFolder))
        cd ../
    end
end

