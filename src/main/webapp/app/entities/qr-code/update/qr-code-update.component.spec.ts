import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { of, Subject, from } from 'rxjs';

import { QRCodeService } from '../service/qr-code.service';
import { IQRCode } from '../qr-code.model';
import { QRCodeFormService } from './qr-code-form.service';

import { QRCodeUpdateComponent } from './qr-code-update.component';

describe('QRCode Management Update Component', () => {
  let comp: QRCodeUpdateComponent;
  let fixture: ComponentFixture<QRCodeUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let qRCodeFormService: QRCodeFormService;
  let qRCodeService: QRCodeService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, RouterTestingModule.withRoutes([]), QRCodeUpdateComponent],
      providers: [
        FormBuilder,
        {
          provide: ActivatedRoute,
          useValue: {
            params: from([{}]),
          },
        },
      ],
    })
      .overrideTemplate(QRCodeUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(QRCodeUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    qRCodeFormService = TestBed.inject(QRCodeFormService);
    qRCodeService = TestBed.inject(QRCodeService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('Should update editForm', () => {
      const qRCode: IQRCode = { id: 456 };

      activatedRoute.data = of({ qRCode });
      comp.ngOnInit();

      expect(comp.qRCode).toEqual(qRCode);
    });
  });

  describe('save', () => {
    it('Should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IQRCode>>();
      const qRCode = { id: 123 };
      jest.spyOn(qRCodeFormService, 'getQRCode').mockReturnValue(qRCode);
      jest.spyOn(qRCodeService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ qRCode });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: qRCode }));
      saveSubject.complete();

      // THEN
      expect(qRCodeFormService.getQRCode).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(qRCodeService.update).toHaveBeenCalledWith(expect.objectContaining(qRCode));
      expect(comp.isSaving).toEqual(false);
    });

    it('Should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IQRCode>>();
      const qRCode = { id: 123 };
      jest.spyOn(qRCodeFormService, 'getQRCode').mockReturnValue({ id: null });
      jest.spyOn(qRCodeService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ qRCode: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: qRCode }));
      saveSubject.complete();

      // THEN
      expect(qRCodeFormService.getQRCode).toHaveBeenCalled();
      expect(qRCodeService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('Should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IQRCode>>();
      const qRCode = { id: 123 };
      jest.spyOn(qRCodeService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ qRCode });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(qRCodeService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });
});
