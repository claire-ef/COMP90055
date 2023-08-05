normalFiles = dir('data/NORMAL/*.jpeg');
pneumoniaFiles = dir('data/PNEUMONIA/*.jpeg');

files = [normalFiles pneumoniaFiles];

for iImg = 1 : numel(files)
    imgPath = strcat(files(iImg).folder,'/', files(iImg).name);
    % read image
    cover = imread(imgPath);
    [nRow, nColumn, npage] = size(cover); 
    if npage ~= 1
        gray = im2gray(cover);
        % save gray image
        path_filetype = split(files(iImg).name,".");
        imwrite(gray,files(iImg).folder+"/"+files(iImg).name, "JPEG");
    end

end