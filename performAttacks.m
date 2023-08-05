normalFiles = dir('data/NORMAL/*.jpeg');
pneumoniaFiles = dir('data/PNEUMONIA/*.jpeg');

files = [normalFiles pneumoniaFiles];
algorithms = ["C4S", "Kumar", "Naseem"];

for iImg = 1 : numel(files)
    path_filetype = split(files(iImg).name, ".");
    for iAlgorithm = 1:numel(algorithms)
        algorithm = algorithms(iAlgorithm);
        if iImg <= 500 % normal
            imgPath = "results/"+algorithm+"/NORMAL/" + path_filetype(1) + "_steg.png";
        else % pneumonia
            imgPath = "results/"+algorithm+"/PNEUMONIA/" + path_filetype(1) + "_steg.png";
        end
        % read cover/stego image
        img = imread(imgPath);
        % perform attacks and save attacked images
        imwrite(imnoise(img, 'gaussian'), "attackedImages/gaussianNoise/"+algorithm+"/"+ path_filetype(1) + "_guassianNoise.png", "PNG");
        imwrite(imnoise(img, 'speckle'), "attackedImages/speckleNoise/"+algorithm+"/"+ path_filetype(1) + "_speckleNoise.png", "PNG");
        imwrite(imnoise(img, 'salt & pepper'), "attackedImages/saltAndPepperNoise/"+algorithm+"/"+ path_filetype(1) + "_saltAndPepperNoise.png", "PNG");
        imwrite(imadjust(img), "attackedImages/contrastAdjustment/"+algorithm+"/"+ path_filetype(1) + "_contrastAdjustment.png", "PNG");
    end
end