function generateFracLacImages()
%
%   Traverse a directory, calling FracLac functions
%   on several all result folders.
%

allFiles=dir(pwd);

for i=1:length(allFiles)
    if length(allFiles(i).name)>10
        folder = char(allFiles(i).name)
        cd(folder)
        pwd
        plotAgents_FracLac(48)
        cd ../
    end
end